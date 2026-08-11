package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.common.command.NetworkInformationCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkInformationCommand : SurfCoreMinestomCommand() {
    @Command("ninfo")
    @CommandPermission(CorePermissions.COMMAND_INFO)
    fun networkInformation(actor: MinestomCommandActor) =
        NetworkInformationCommandHandler.sendNetworkInformation(actor.sender())

    @Command("ninfo player")
    @CommandPermission(CorePermissions.COMMAND_INFO_PLAYER)
    fun playerInformation(actor: MinestomCommandActor, target: SurfPlayer) =
        NetworkInformationCommandHandler.sendPlayerInformation(actor.sender(), target)

    @Command("ninfo server")
    @CommandPermission(CorePermissions.COMMAND_INFO_SERVER)
    fun serverInformation(actor: MinestomCommandActor, theServer: CommonSurfServer) =
        NetworkInformationCommandHandler.sendServerInformation(actor.sender(), theServer)
}
