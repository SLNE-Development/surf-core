package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.minestom.command.argument.PermissionSurfServer
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkServerCommand : SurfCoreMinestomCommand() {
    @Command("nserver")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SERVER)
    fun sendSelf(
        actor: MinestomCommandActor,
        @PermissionSurfServer server: CommonSurfServer,
    ) {
        val player = actor.requirePlayer()
        minestomScope.launch {
            NetworkSendCommandHandler.sendSelf(player, player.surfPlayer, server)
        }
    }
}
