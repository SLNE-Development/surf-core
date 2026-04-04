package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun whereAmICommand() = commandTree("whereami") {
    withPermission(PermissionRegistry.COMMAND_WHERE_AM_I)
    playerExecutor { player, _ ->
        val surfPlayer =
            SurfPlayerService.players.firstOrNull { it.uuid == player.uniqueId } ?: run {
                player.sendText {
                    appendCorePrefix()
                    error("Deine Spielerdaten konnten nicht geladen werden.")
                }
                return@playerExecutor
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
            variableValue(surfPlayer.currentServer?.name ?: "Unbekannt")
            info(" auf dem Proxy ")
            variableValue(surfPlayer.currentProxy?.name ?: "Unbekannt")
            info(".")
        }
    }
}