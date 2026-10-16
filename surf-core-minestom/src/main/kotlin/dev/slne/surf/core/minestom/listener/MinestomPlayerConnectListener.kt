package dev.slne.surf.core.minestom.listener

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.api.minestom.player.event.AsyncPlayerCountEvent
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.core.common.player.PlayerConnectionMessages
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.minestom.MinestomPlayerDisplayNameService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object MinestomPlayerConnectListener : EventRegistrar {
    private val log = logger()

    override fun register(node: EventNode<Event>) {
        node.addListener<AsyncPlayerCountEvent>(::onPlayerCountCheck)
        node.addListener<AsyncPlayerConfigurationEvent>(::onPlayerConfiguration)
        node.addListener<PlayerSpawnEvent>(::onPlayerSpawn)
    }

    private fun onPlayerCountCheck(event: AsyncPlayerCountEvent) {
        if (!event.player.hasPermission(CorePermissions.BYPASS_MAX_PLAYERS)) {
            return
        }

        event.allow()
    }

    private fun onPlayerConfiguration(event: AsyncPlayerConfigurationEvent) {
        if (!event.isFirstConfig) {
            return
        }

        val player = event.player

        val available = runCatching {
            runBlocking {
                withTimeout(2.seconds) {
                    val remote = SurfPlayerService.findPlayerByUuidRemote(player.uuid)
                        ?: return@withTimeout false

                    val expectedSessionId = remote.connectionSessionId

                    while (true) {
                        val local = SurfPlayerService.findPlayerByUuid(player.uuid)

                        if (expectedSessionId != null) {
                            if (local?.connectionSessionId == expectedSessionId) {
                                return@withTimeout true
                            }
                        } else if (local != null) {
                            return@withTimeout true
                        }

                        delay(20.milliseconds)
                    }

                    @Suppress("KotlinUnreachableCode")
                    true
                }
            }
        }.getOrElse { throwable ->
            log.atWarning()
                .withCause(throwable)
                .log(
                    "Failed to verify Redis player state for %s during Minestom configuration",
                    player.uuid,
                )

            false
        }

        if (available) {
            return
        }

        val errorCode = SurfPlayerErrorService.saveError(
            playerUuid = player.uuid,
            staffMessage = "Player state could not be verified in Redis while connecting to a Minestom server.",
            scope = minestomScope,
        )

        player.kick(PlayerConnectionMessages.dataLoadFailure(errorCode))
    }

    private fun onPlayerSpawn(event: PlayerSpawnEvent) {
        if (!event.isFirstSpawn) return

        val player = event.player
        val surfPlayer = SurfPlayerService.findPlayerByUuid(player.uuid) ?: return

        MinestomPlayerDisplayNameService.update(player)

        if (player.hasPermission(CorePermissions.JOIN_WHERE_AM_I)) {
            WhereAmICommandHandler.send(player, player.uuid, player.username)
        }

        if (!surfPlayer.transferred) {
            return
        }

        CoreInstance.redisApi.publishEvent(
            SendPlayerToProxyRequest.Response(
                surfPlayer.uuid,
                SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.SUCCESS),
            )
        )
        minestomScope.launch {
            PlayerProxyConnectionResultWatcher.cleanUp(surfPlayer.uuid)
        }
    }
}
