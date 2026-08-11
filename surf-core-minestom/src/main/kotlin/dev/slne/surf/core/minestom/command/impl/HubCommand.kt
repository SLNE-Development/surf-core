package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.HubCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class HubCommand : SurfCoreMinestomCommand() {
    @Command("hub", "lobby", "l")
    @CommandPermission(CorePermissions.COMMAND_HUB)
    fun hub(actor: MinestomCommandActor) {
        val player = actor.requirePlayer()
        minestomScope.launch {
            HubCommandHandler.sendToHub(player, player.surfPlayer)
        }
    }
}
