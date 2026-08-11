package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.command.SurfCoreMinestomCommand
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class WhereAmICommand : SurfCoreMinestomCommand() {
    @Command("whereami")
    @CommandPermission(CorePermissions.COMMAND_WHERE_AM_I)
    fun whereAmI(actor: MinestomCommandActor) {
        val player = actor.requirePlayer()
        WhereAmICommandHandler.send(player, player.uuid, player.username)
    }
}
