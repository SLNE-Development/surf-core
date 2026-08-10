package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.core.core.common.command.NetworkServerCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkServerMaxPlayersCommand() = commandTree("nmaxplayers") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER_MAX_PLAYERS)

    surfBackendServerArgument("backend") {
        integerArgument("maxPlayers") {
            anyExecutor { sender, args ->
                val backend: CommonSurfServer by args
                val maxPlayers: Int by args

                NetworkServerCommandHandler.changeMaxPlayers(sender, backend, maxPlayers)
            }
        }
    }
}
