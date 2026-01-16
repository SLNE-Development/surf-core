package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkInformationCommand() = commandTree("ninfo") {
    withPermission(PermissionRegistry.COMMAND_INFO)

    anyExecutor { sender, _ ->
        sender.sendText {
            appendPrefix()
            info("Derzeit sind ")
            variableValue(surfPlayerService.players.size)
            info(" Spieler verteilt auf ")
            variableValue("???")
            info(" Server online.")
        }
    }

    literalArgument("player") {
        withPermission(PermissionRegistry.COMMAND_INFO_PLAYER)
        surfPlayerArgument("target") {
            anyExecutor { executor, args ->
                val target: SurfPlayer by args

                executor.sendText {
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.lastKnownName ?: target.uuid.toString())

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Erstes mal gesehen:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.firstSeen?.formatMillis() ?: "/")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Aktueller Server:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.currentServer ?: "Unbekannt")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Aktueller Proxy:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.currentProxy ?: "Unbekannt")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Letzte Ip-Adresse:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.lastKnownIpAddress.toString() ?: "/")
                }
            }
        }
    }

    literalArgument("server") {
        withPermission(PermissionRegistry.COMMAND_INFO_SERVER)
        anyExecutor { sender, _ ->
            sender.sendText {
                appendPrefix()
                error("Die Server Api ist nocht nicht fertig... lg red ~ 16.01.26")
            }
        }
    }
}