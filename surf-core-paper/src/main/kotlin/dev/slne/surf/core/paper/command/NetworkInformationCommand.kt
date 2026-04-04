package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.formatDateTime
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkInformationCommand() = commandTree("ninfo") {
    withPermission(PermissionRegistry.COMMAND_INFO)

    anyExecutor { sender, _ ->
        sender.sendText {
            appendCorePrefix()
            info("Derzeit sind ")
            variableValue(SurfPlayerService.players.size)
            info(" Spieler verteilt auf ")
            variableValue(SurfServerService.servers.size)
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
        surfServerArgument("commonSurfServer") {
            anyExecutor { executor, args ->
                val commonSurfServer: CommonSurfServer by args

                executor.sendText {
                    appendNewline()
                    darkSpacer("» | ")
                    variableValue(commonSurfServer.name)

                    appendNewline()
                    darkSpacer("» | ")

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Kategorie: ")
                    variableValue(commonSurfServer.category)

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Status: ")
                    variableValue(commonSurfServer.state.toString())

                    appendNewline()
                    darkSpacer("» | ")
                    variableKey("Spieler: ")
                    variableValue("${commonSurfServer.getPlayerCount()}/${commonSurfServer.maxPlayers}")
                }
            }
        }
    }
}