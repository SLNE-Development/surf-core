package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun networkSendCommand() = commandTree("nsend") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SEND)

    literalArgument("player") {
        surfPlayerArgument("player") {
            surfServerArgument("server") {
                anyExecutor { executor, args ->
                    val player: SurfPlayer by args
                    val server: CommonSurfServer by args

                    plugin.launch {
                        NetworkSendCommandHandler.sendPlayer(executor, player, server)
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
                    plugin.launch {
                        NetworkSendCommandHandler.sendPlayers(executor, server.getPlayers(), targetServer, server.name)
                    }
                }
            }
        }
    }

    literalArgument("all") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args
                plugin.launch {
                    NetworkSendCommandHandler.sendPlayers(
                        executor,
                        SurfCoreApi.getOnlinePlayers(),
                        targetServer,
                        "global"
                    )
                }
            }
        }
    }

    literalArgument("current") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args
                val current = SurfServer.current()

                plugin.launch {
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
}

