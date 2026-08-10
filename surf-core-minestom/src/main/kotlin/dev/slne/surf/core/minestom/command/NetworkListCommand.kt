package dev.slne.surf.core.minestom.command

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.core.common.command.NetworkListCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkListCommand : SurfCoreMinestomCommand() {
    @Command("nlist")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_LIST)
    fun networkList(actor: MinestomCommandActor) = NetworkListCommandHandler.send(actor.sender())
}
