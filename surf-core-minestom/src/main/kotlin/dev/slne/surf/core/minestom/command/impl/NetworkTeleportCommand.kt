package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.util.minestomPlayer
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class NetworkTeleportCommand : SurfCoreMinestomCommand() {
    @Command("ntp")
    @CommandPermission(CorePermissions.COMMAND_NETWORK_TELEPORT)
    fun teleport(actor: MinestomCommandActor, target: SurfPlayer) {
        val player = actor.requirePlayer()
        minestomScope.launch {
            NetworkTeleportCommandHandler.teleport(player, player.surfPlayer, target) {
                val targetPlayer = target.minestomPlayer ?: return@teleport false
                player.teleport(targetPlayer.position).await()
                NetworkTeleportCommandHandler.sendSuccess(player, targetPlayer.username)
                true
            }
        }
    }
}
