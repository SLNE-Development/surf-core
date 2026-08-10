package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.command.NetworkInformationCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkInformationCommand() = commandTree("ninfo") {
    withPermission(PermissionRegistry.COMMAND_INFO)

    anyExecutor { sender, _ ->
        NetworkInformationCommandHandler.sendNetworkInformation(sender)
    }

    literalArgument("player") {
        withPermission(PermissionRegistry.COMMAND_INFO_PLAYER)
        surfPlayerArgument("target") {
            anyExecutor { executor, args ->
                val target: SurfPlayer by args

                NetworkInformationCommandHandler.sendPlayerInformation(executor, target)
            }
        }
    }

    literalArgument("server") {
        withPermission(PermissionRegistry.COMMAND_INFO_SERVER)
        surfServerArgument("commonSurfServer") {
            anyExecutor { executor, args ->
                val commonSurfServer: CommonSurfServer by args

                NetworkInformationCommandHandler.sendServerInformation(executor, commonSurfServer)
            }
        }
    }
}
