package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutor
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.integerArgument
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.core.core.common.command.NetworkServerCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun networkServerMaxPlayersCommand() = commandTree("nmaxplayers") {
    withPermission(CorePermissions.COMMAND_NETWORK_SERVER_MAX_PLAYERS)

    surfBackendServerArgument("backend") {
        integerArgument("maxPlayers", min = 1) {
            anyExecutor { sender, args ->
                val backend: CommonSurfServer by args
                val maxPlayers: Int by args

                NetworkServerCommandHandler.changeMaxPlayers(sender, backend, maxPlayers)
            }
        }
    }
}
