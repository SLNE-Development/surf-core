package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience

object NetworkListCommandHandler {
    fun send(audience: Audience) {
        val players = SurfPlayerService.players.sortedBy { it.lastKnownName }
        if (players.isEmpty()) {
            audience.sendText {
                appendCorePrefix()
                error("Es sind derzeit keine Spieler im Netzwerk online.")
            }
            return
        }

        val serverGroups = players.mapNotNull { player ->
            player.currentServerName?.let { it to player }
        }.groupBy({ it.first }, { it.second })
        val proxyGroups = players.mapNotNull { player ->
            player.currentProxyName?.let { it to player }
        }.groupBy({ it.first }, { it.second })

        audience.sendText {
            appendCorePrefix()
            info("Es sind derzeit ")
            variableValue(players.size)
            info(" Spieler auf dem Netzwerk online:\n")

            (serverGroups.entries + proxyGroups.entries).forEach { (server, list) ->
                spacer("- ")
                variableValue(server)
                info(" (")
                variableValue(list.size)
                info("): ")
                variableValue(list.joinToString(", ") { it.lastKnownName ?: it.uuid.toString() })
                info("\n")
            }
        }
    }
}
