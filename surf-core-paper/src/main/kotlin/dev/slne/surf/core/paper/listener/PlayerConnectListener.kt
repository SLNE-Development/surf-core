package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.core.messages.adventure.uuid
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.api.paper.command.util.idOrThrow
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.core.common.player.PlayerConnectionMessages
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.jetbrains.annotations.Blocking
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object PlayerConnectListener : Listener {
    private val luckperms by lazy {
        LuckPermsProvider.get()
    }

    @EventHandler
    fun onPlayerClientLoaded(event: PlayerClientLoadedWorldEvent) {
        val player = event.player
        val surfPlayer = SurfPlayerService.findPlayerByUuid(player.uniqueId)

        if (surfPlayer == null) {
            player.kick(
                PlayerConnectionMessages.dataLoadFailure(
                    SurfPlayerErrorService.saveError(
                        playerUuid = player.uniqueId,
                        staffMessage = "Player not found in redis after connecting to a server, most likely a redis issue. Is redis down?",
                        scope = plugin.scope
                    )
                ), PlayerKickEvent.Cause.UNKNOWN
            )
            return
        }

        if (!event.player.hasPermission(PermissionRegistry.JOIN_WHERE_AM_I)) {
            return
        }

        WhereAmICommandHandler.send(player, player.uniqueId, player.name)
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun onPlayerConnect(event: AsyncPlayerConnectionConfigureEvent) {
        val uuid = event.connection.audience.uuid()

        if (awaitPlayerState(uuid)) {
            return
        }

        plugin.logger.severe(
            "Failed to load player data for player with UUID ${event.connection.audience.uuidOrNull()}. " +
                    "The player will be disconnected."
        )

        event.connection.disconnect(
            PlayerConnectionMessages.dataLoadFailure(
                SurfPlayerErrorService.saveError(
                    playerUuid = uuid,
                    staffMessage = "Player state could not be verified in Redis while connecting to a server.",
                    scope = plugin.scope,
                )
            )
        )
    }

    @EventHandler
    fun onPlayerServerFullCheckEvent(event: PlayerServerFullCheckEvent) {
        val uuid = event.playerProfile.idOrThrow()
        val user = luckperms.userManager.getUser(uuid) ?: return
        val hasBypass = user.cachedData.permissionData.checkPermission(
            PermissionRegistry.BYPASS_MAX_PLAYERS
        ).asBoolean()

        if (hasBypass) {
            event.allow(true)
        }
    }

    @EventHandler
    fun onTransferred(event: PlayerJoinEvent) {
        val player = event.player.surfPlayer

        if (!player.transferred) {
            return
        }

        CoreInstance.redisApi.publishEvent(
            SendPlayerToProxyRequest.Response(
                player.uuid,
                SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.SUCCESS)
            )
        )

        plugin.launch {
            PlayerProxyConnectionResultWatcher.cleanUp(player.uuid)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.displayName(event.player.getPrefixedName())
    }

    @Blocking
    private fun awaitPlayerState(uuid: UUID): Boolean {
        return runCatching {
            runBlocking {
                withTimeout(2.seconds) {
                    val remote = SurfPlayerService.findPlayerByUuidRemote(uuid)
                        ?: return@withTimeout false

                    val expectedSessionId = remote.connectionSessionId

                    while (true) {
                        val local = SurfPlayerService.findPlayerByUuid(uuid)

                        if (expectedSessionId != null) {
                            if (local?.connectionSessionId == expectedSessionId) {
                                return@withTimeout true
                            }
                        } else if (local != null) {
                            // Compatibility with player states written before session IDs existed.
                            return@withTimeout true
                        }

                        delay(20.milliseconds)
                    }

                    @Suppress("KotlinUnreachableCode")
                    true
                }
            }
        }.getOrElse { throwable ->
            plugin.componentLogger.warn(
                "Failed to verify Redis player state for $uuid during configuration",
                throwable,
            )

            false
        }
    }
}
