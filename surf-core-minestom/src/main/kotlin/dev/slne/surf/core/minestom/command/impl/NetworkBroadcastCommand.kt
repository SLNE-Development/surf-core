package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.core.common.command.NetworkBroadcastCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkBroadcastCommand : SurfCoreMinestomCommand() {
    @Command("nbroadcast")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_BROADCAST)
    fun broadcast(actor: MinestomCommandActor, message: String) =
        NetworkBroadcastCommandHandler.broadcast(actor.sender(), message)
}
