package dev.slne.surf.core.minestom.command.impl

import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
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
        surfPlayerArgument("target") {
            surfServerArgument("targetServer") {
                anyExecutorSuspend { executor, args ->
                    val target: SurfPlayer by args
                    val targetServer: CommonSurfServer by args
                    NetworkSendCommandHandler.sendPlayer(executor, target, targetServer)
                }
            }
        }
    }

    literalArgument("server") {
        surfServerArgument("sourceServer") {
            surfServerArgument("targetServer") {
                anyExecutorSuspend { executor, args ->
                    val sourceServer: CommonSurfServer by args
                    val targetServer: CommonSurfServer by args
                    NetworkSendCommandHandler.sendPlayers(
                        executor,
                        sourceServer.getPlayers(),
                        targetServer,
                        sourceServer.name
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