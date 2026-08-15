package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.minestom.command.argument.permissionSurfServerArgument
import dev.slne.surf.core.api.minestom.util.toSurfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun networkServerCommand() = commandTree("nserver") {
    withPermission(CorePermissions.COMMAND_NETWORK_SERVER)

    permissionSurfServerArgument("server") {
        playerExecutorSuspend { player, args ->
            val server: CommonSurfServer by args
            val surfPlayer = player.toSurfPlayer()

            NetworkSendCommandHandler.sendSelf(player, surfPlayer, server)
        }
    }
}
