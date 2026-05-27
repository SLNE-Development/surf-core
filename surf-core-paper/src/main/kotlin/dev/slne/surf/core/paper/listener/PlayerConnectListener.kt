package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.*
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.command.util.idOrThrow
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.core.common.util.niceRed
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent

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
                buildDisconnectComponent(
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

        player.sendText {
            appendCorePrefix()
            info("Du, ")
            append {
                variableValue(player.name)
                hoverEvent(buildText {
                    variableValue(player.uniqueId.toString())
                })
                clickCopiesToClipboard(player.uniqueId.toString())
            }
            info(", befindest dich momentan, ")
            append {
                spacer("(${System.currentTimeMillis().formatMillis()})")
                clickCopiesToClipboard(System.currentTimeMillis().toString())
            }
            info(", auf dem Server ")
            variableValue(surfPlayer.currentServerName ?: "Unbekannt")
            info(" auf dem Proxy ")
            variableValue(surfPlayer.currentProxyName ?: "Unbekannt")
            info(".")
        }
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun onPlayerConnect(event: AsyncPlayerConnectionConfigureEvent) {
        val uuid = event.connection.audience.uuid()
        val surfPlayer = SurfPlayerService.findPlayerByUuid(uuid)

        if (surfPlayer == null) {
            plugin.logger.severe("Failed to load player data for player with UUID ${event.connection.audience.uuidOrNull()}. The player will be disconnected.")
            event.connection.disconnect(
                buildDisconnectComponent(
                    SurfPlayerErrorService.saveError(
                        playerUuid = uuid,
                        staffMessage = "Player not found in redis after connecting to a server, most likely a redis issue. Is redis down?",
                        scope = plugin.scope
                    )
                )
            )
        }
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

    private fun buildDisconnectComponent(errorCode: String) =
        CommonComponents.renderDisconnectMessage(
            SurfComponentBuilder(),
            "DEINE SPIELERDATEN KONNTEN NICHT GELADEN WERDEN.",
            {
                spacer("Fehlercode: ")
                niceRed(errorCode)
                appendNewline()
                error("Internal Server error. Data Transmitter or holder may be down?")
                appendNewline(3)
                spacer("Beim laden deiner Spielerdaten ist ein interner Fehler aufgetreten.")
            },
            issue = true
        )
}