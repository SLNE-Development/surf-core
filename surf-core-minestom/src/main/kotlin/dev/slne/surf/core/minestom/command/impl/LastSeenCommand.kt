package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.minestom.command.argument.SurfOfflinePlayerArgument
import dev.slne.surf.core.core.common.command.LastSeenCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class LastSeenCommand : SurfCoreMinestomCommand() {
    @Command("lastseen")
    @CommandPermission(CorePermissions.COMMAND_LAST_SEEN)
    fun lastSeen(actor: MinestomCommandActor, player: SurfOfflinePlayerArgument) {
        minestomScope.launch {
            LastSeenCommandHandler.send(actor.sender(), player.resolve())
        }
    }
}
