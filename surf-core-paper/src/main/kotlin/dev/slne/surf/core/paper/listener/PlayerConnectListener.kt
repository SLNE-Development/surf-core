package dev.slne.surf.core.paper.listener

import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.core.common.util.renderDisconnectMessage
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerKickEvent

object PlayerConnectListener : Listener {
    @EventHandler
    fun onPlayerClientLoaded(event: PlayerClientLoadedWorldEvent) {
        val player = event.player
        val surfPlayer = surfPlayerService.findPlayerByUuid(player.uniqueId)

        if (surfPlayer == null) {
            val code = surfCoreApi.logError(
                player.uniqueId,
                "Failed to load player data on PlayerClientLoadedWorldEvent"
            )
            player.kick(buildDisconnectComponent(code), PlayerKickEvent.Cause.UNKNOWN)
            return
        }

        if (!event.player.hasPermission(PermissionRegistry.JOIN_WHERE_AM_I)) {
            return
        }

        player.sendText {
            appendInfoPrefix()
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
            variableValue(surfPlayer.currentServer?.name ?: "Unbekannt")
            info(" auf dem Proxy ")
            variableValue(surfPlayer.currentProxy?.name ?: "Unbekannt")
            info(".")
        }
    }

    @EventHandler
    fun onPlayerConnect(event: AsyncPlayerPreLoginEvent) {
        val surfPlayer = surfPlayerService.findPlayerByUuid(event.uniqueId)

        if (surfPlayer == null) {
            val code = surfCoreApi.logError(
                event.uniqueId,
                "Failed to load player data on AsyncPlayerPreLoginEvent"
            )
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                buildDisconnectComponent(code)
            )
        }
    }

    private fun buildDisconnectComponent(code: String) = buildText {
        renderDisconnectMessage("DEINE SPIELERDATEN KONNTEN NICHT GELADEN WERDEN", {
            spacer("Code: ")
            error("#$code")
            appendNewline()
            spacer("Beim laden deiner Spielerdaten ist ein interner Fehler aufgetreten.")
        })
    }
}