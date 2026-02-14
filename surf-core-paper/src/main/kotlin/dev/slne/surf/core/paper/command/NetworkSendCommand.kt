package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkSendCommand() = commandTree("nsend") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SEND)

    literalArgument("player") {
        surfPlayerArgument("player") {
            surfServerArgument("server") {
                anyExecutor { executor, args ->
                    val player: SurfPlayer by args
                    val server: CommonSurfServer by args

                    player.send(server)

                    executor.sendText {
                        appendSuccessPrefix()
                        success("Der Spieler ")
                        variableValue(player.lastKnownName ?: "Unbekannt")
                        success(" wurde zum Server ")
                        variableValue(server.name)
                        success(" gesendet.")
                    }
                }
            }
        }
    }

    literalArgument("server") {
        surfServerArgument("server") {
            surfServerArgument("targetServer") {
                anyExecutor { executor, args ->
                    val server: CommonSurfServer by args
                    val targetServer: CommonSurfServer by args

                    val amount = server.getPlayerCount()

                    server.getPlayers().forEach {
                        it.send(targetServer)
                    }

                    executor.sendText {
                        appendSuccessPrefix()
                        success("Die Spieler wurden vom Server ")
                        variableValue(server.name)
                        success(" zum Server ")
                        variableValue(targetServer.name)
                        success(" gesendet.")
                    }
                }
            }
        }
    }

    literalArgument("all") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args

                val amount = surfCoreApi.getOnlinePlayers().size

                surfCoreApi.getOnlinePlayers().forEach {
                    it.send(targetServer)
                }

                executor.sendText {
                    appendSuccessPrefix()
                    variableValue(amount)
                    success(" Spieler wurden zum Server ")
                    variableValue(targetServer.name)
                    success(" gesendet.")
                }
            }
        }
    }

    literalArgument("current") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args
                val currentServer = SurfServer.current()

                val amount = currentServer.getPlayers().size

                currentServer.getPlayers().forEach {
                    it.send(targetServer)
                }

                executor.sendText {
                    appendSuccessPrefix()
                    variableValue(amount)
                    success(" Spieler wurden vom Server ")
                    variableValue(currentServer.name)
                    success(" zum Server ")
                    variableValue(targetServer.name)
                    success(" gesendet.")
                }
            }
        }
    }
}