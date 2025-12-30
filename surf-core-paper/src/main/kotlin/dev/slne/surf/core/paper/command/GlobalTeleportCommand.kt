package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun globalTeleportCommand() = commandTree("gtp") {//TODO: Permission
    surfPlayerArgument("target") {
        playerExecutor { player, args ->
            val target: SurfPlayer by args
            val targetServer = target.currentServer ?: run {
                player.sendText {
                    appendPrefix()
                    error("Der Spieler ist auf keinem Server.")
                }
                return@playerExecutor
            }
        }
    }
}