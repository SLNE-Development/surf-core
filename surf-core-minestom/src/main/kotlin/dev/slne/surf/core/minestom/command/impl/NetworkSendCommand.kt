package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkSendCommand : SurfCoreMinestomCommand() {
    @Command("nsend player")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SEND)
    fun sendPlayer(
        actor: MinestomCommandActor,
        targetPlayer: SurfPlayer,
        targetServer: CommonSurfServer,
    ) {
        minestomScope.launch {
            NetworkSendCommandHandler.sendPlayer(actor.sender(), targetPlayer, targetServer)
        }
    }

    @Command("nsend server")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SEND)
    fun sendServer(
        actor: MinestomCommandActor,
        source: CommonSurfServer,
        target: CommonSurfServer,
    ) {
        minestomScope.launch {
            NetworkSendCommandHandler.sendPlayers(
                actor.sender(),
                source.getPlayers(),
                target,
                source.name,
            )
        }
    }

    @Command("nsend all")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SEND)
    fun sendAll(actor: MinestomCommandActor, target: CommonSurfServer) {
        minestomScope.launch {
            NetworkSendCommandHandler.sendPlayers(
                actor.sender(),
                SurfCoreApi.getOnlinePlayers(),
                target,
                "global",
            )
        }
    }

    @Command("nsend current")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_SEND)
    fun sendCurrent(actor: MinestomCommandActor, target: CommonSurfServer) {
        val current = SurfServer.current()
        minestomScope.launch {
            NetworkSendCommandHandler.sendPlayers(
                actor.sender(),
                current.getPlayers(),
                target,
                current.name,
            )
        }
    }
}
