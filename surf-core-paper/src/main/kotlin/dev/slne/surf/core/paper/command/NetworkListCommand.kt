package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkListCommand() = commandTree("nlist") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_LIST)
    anyExecutor { executor, _ ->
        val players = SurfPlayerService.players.sortedBy { it.lastKnownName }

        if (players.isEmpty()) {
            executor.sendText {
                appendCorePrefix()
                error("Es sind derzeit keine Spieler im Netzwerk online.")
            }
            return@anyExecutor
        }

        val serverGroups = players
            .mapNotNull { player ->
                player.currentServer?.name?.let { it to player }
            }
            .groupBy({ it.first }, { it.second })

        val proxyGroups = players
            .mapNotNull { player ->
                player.currentProxy?.name?.let { it to player }
            }
            .groupBy({ it.first }, { it.second })

        executor.sendText {
            appendCorePrefix()
            info("Es sind derzeit ")
            variableValue(players.size)
            info(" Spieler auf dem Netzwerk online:\n")

            serverGroups.forEach { (server, list) ->
                spacer("- ")
                variableValue(server)
                info(" (")
                variableValue(list.size)
                info("): ")
                variableValue(list.joinToString(", ") { it.lastKnownName ?: it.uuid.toString() })
                info("\n")
            }

            proxyGroups.forEach { (proxy, list) ->
                spacer("- ")
                variableValue(proxy)
                info(" (")
                variableValue(list.size)
                info("): ")
                variableValue(list.joinToString(", ") { it.lastKnownName ?: it.uuid.toString() })
                info("\n")
            }
        }
    }
}

