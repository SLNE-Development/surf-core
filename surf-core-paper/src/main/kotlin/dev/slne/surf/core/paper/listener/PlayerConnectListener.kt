package dev.slne.surf.core.paper.listener

import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.util.formatMillis
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
            player.kick(buildDisconnectComponent(), PlayerKickEvent.Cause.UNKNOWN)
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
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, buildDisconnectComponent())
        }
    }

    private fun buildDisconnectComponent() = buildText {
        appendNewline()
        primary("CASTCRAFTER")
        appendNewline()
        primary("COMMUNITY SERVER")
        appendNewline()
        appendNewline()
        error("DEINE DATEN KONNTEN NICHT GELADEN WERDEN.")
        appendNewline()
        error("Code: 1921186215185: 1") // surf-core in A1Z26-Cipher + Error Code 1
        appendNewline()
        appendNewline()
        appendNewline()
        spacer("Beim laden deiner Daten ist ein interner Fehler aufgetreten.")
        appendNewline()
        spacer("Sollte das Problem weiterhin bestehen, wende dich bitte an den Support.")
        appendNewline()
        appendNewline()
        primary("discord.gg/castcrafter")
    }
}