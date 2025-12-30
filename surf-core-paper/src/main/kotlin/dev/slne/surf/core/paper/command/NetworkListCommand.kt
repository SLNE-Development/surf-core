package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkListCommand() = commandTree("nlist") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_LIST)
    anyExecutor { executor, _ ->
        val players = surfPlayerService.players.sortedBy { it.lastKnownName }

        if (players.isEmpty()) {
            executor.sendText {
                appendPrefix()
                error("Es sind keine Spieler im Netzwerk online.")
            }
            return@anyExecutor
        }

        executor.sendText {
            appendPrefix()
            info("Derzeit sind ")
            variableValue(players.size)
            info(" Spieler auf dem Netzwerk online: ")
            variableValue(players.joinToString(", ") { it.lastKnownName ?: it.uuid.toString() })
        }
    }
}