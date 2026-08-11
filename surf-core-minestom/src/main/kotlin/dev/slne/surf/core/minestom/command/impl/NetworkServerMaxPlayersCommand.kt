package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.command.NetworkServerCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkServerMaxPlayersCommand : SurfCoreMinestomCommand() {
    @Command("nmaxplayers")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SERVER_MAX_PLAYERS)
    fun maxPlayers(actor: MinestomCommandActor, server: SurfServer, maxPlayers: Int) {
        NetworkServerCommandHandler.changeMaxPlayers(actor.sender(), server, maxPlayers)
    }
}
