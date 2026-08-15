package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutorSuspend
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun networkSendCommand() = commandTree("nsend") {
    withPermission(CorePermissions.COMMAND_NETWORK_SEND)

    literalArgument("player") {
        surfPlayerArgument("player") {
            surfServerArgument("server") {
                anyExecutorSuspend { executor, args ->
                    val player: SurfPlayer by args
                    val server: CommonSurfServer by args
                    NetworkSendCommandHandler.sendPlayer(executor, player, server)
                }
            }
        }
    }

    literalArgument("server") {
        surfServerArgument("server") {
            surfServerArgument("targetServer") {
                anyExecutorSuspend { executor, args ->
                    val server: CommonSurfServer by args
                    val targetServer: CommonSurfServer by args
                    NetworkSendCommandHandler.sendPlayers(
                        executor,
                        server.getPlayers(),
                        targetServer,
                        server.name
                    )
                }
            }
        }
    }

    literalArgument("all") {
        surfServerArgument("targetServer") {
            anyExecutorSuspend { executor, args ->
                val targetServer: CommonSurfServer by args
                NetworkSendCommandHandler.sendPlayers(
                    executor,
                    SurfCoreApi.getOnlinePlayers(),
                    targetServer,
                    "global"
                )
            }
        }
    }

    literalArgument("current") {
        surfServerArgument("targetServer") {
            anyExecutorSuspend { executor, args ->
                val targetServer: CommonSurfServer by args
                val current = SurfServer.current()

                NetworkSendCommandHandler.sendPlayers(
                    executor,
                    current.getPlayers(),
                    targetServer,
                    current.name
                )
            }
        }
    }
}