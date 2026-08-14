package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutor
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.command.NetworkInformationCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun networkInformationCommand() = commandTree("ninfo") {
    withPermission(CorePermissions.COMMAND_INFO)

    anyExecutor { sender, _ ->
        NetworkInformationCommandHandler.sendNetworkInformation(sender)
    }

    literalArgument("player") {
        withPermission(CorePermissions.COMMAND_INFO_PLAYER)
        surfPlayerArgument("target") {
            anyExecutor { executor, args ->
                val target: SurfPlayer by args
                NetworkInformationCommandHandler.sendPlayerInformation(executor, target)
            }
        }
    }

    literalArgument("server") {
        withPermission(CorePermissions.COMMAND_INFO_SERVER)
        surfServerArgument("commonSurfServer") {
            anyExecutor { executor, args ->
                val commonSurfServer: CommonSurfServer by args
                NetworkInformationCommandHandler.sendServerInformation(executor, commonSurfServer)
            }
        }
    }
}
