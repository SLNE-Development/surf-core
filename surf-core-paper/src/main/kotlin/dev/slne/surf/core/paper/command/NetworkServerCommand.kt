package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.command.argument.permissionSurfServerArgument
import dev.slne.surf.core.api.paper.util.toSurfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun networkServerCommand() = commandTree("nserver") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER)

    permissionSurfServerArgument("server") {
        playerExecutor { player, args ->
            val server: CommonSurfServer by args
            val surfPlayer = player.toSurfPlayer()

            plugin.launch {
                NetworkSendCommandHandler.sendSelf(player, surfPlayer, server)
            }
        }
    }
}
