package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun whereAmICommand() = commandTree("whereami") {
    withPermission(PermissionRegistry.COMMAND_WHERE_AM_I)
    playerExecutor { player, _ ->
        val surfPlayer =
            surfPlayerService.players.firstOrNull { it.uuid == player.uniqueId } ?: run {
                player.sendText {
                    appendErrorPrefix()
                    error("Deine Spielerdaten konnten nicht geladen werden.")
                }
                return@playerExecutor
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
}