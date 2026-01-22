package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.core.common.util.formatDateTime
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkInformationCommand() = commandTree("ninfo") {
    withPermission(PermissionRegistry.COMMAND_INFO)

    anyExecutor { sender, _ ->
        sender.sendText {
            appendInfoPrefix()
            info("Derzeit sind ")
            variableValue(surfPlayerService.players.size)
            info(" Spieler verteilt auf ")
            variableValue(surfServerService.servers.size)
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
                    variableValue(target.firstSeen?.formatDateTime() ?: "/")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Aktueller Server:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.currentServer?.name ?: "Unbekannt")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Aktueller Proxy:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.currentProxy?.name ?: "Unbekannt")

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Letzte Ip-Adresse:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(target.lastKnownIpAddress.toString())
                }
            }
        }
    }

    literalArgument("server") {
        withPermission(PermissionRegistry.COMMAND_INFO_SERVER)
        surfServerArgument("surfServer") {
            anyExecutor { executor, args ->
                val surfServer: SurfServer by args

                executor.sendText {
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(surfServer.name)

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Kategorie:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(surfServer.category)

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Status:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(surfServer.state.toString())

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Spieler:")
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(surfServer.getPlayerCount())
                }
            }
        }
    }
}