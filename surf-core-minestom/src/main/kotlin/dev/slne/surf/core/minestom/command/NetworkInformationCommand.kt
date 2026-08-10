package dev.slne.surf.core.minestom.command

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.common.command.NetworkInformationCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkInformationCommand : SurfCoreMinestomCommand() {
    @Command("ninfo")
    @CommandPermission(CorePermissions.COMMAND_INFO)
    fun networkInformation(actor: MinestomCommandActor) =
        NetworkInformationCommandHandler.sendNetworkInformation(actor.sender())

    @Command("ninfo player")
    @CommandPermission(CorePermissions.COMMAND_INFO_PLAYER)
    fun playerInformation(actor: MinestomCommandActor, player: SurfPlayer) =
        NetworkInformationCommandHandler.sendPlayerInformation(actor.sender(), player)

    @Command("ninfo server")
    @CommandPermission(CorePermissions.COMMAND_INFO_SERVER)
    fun serverInformation(actor: MinestomCommandActor, server: CommonSurfServer) =
        NetworkInformationCommandHandler.sendServerInformation(actor.sender(), server)
}
