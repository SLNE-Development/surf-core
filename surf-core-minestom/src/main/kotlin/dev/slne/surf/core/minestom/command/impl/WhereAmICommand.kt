package dev.slne.surf.core.minestom.command.impl

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun whereAmICommand() = commandTree("whereami") {
    withPermission(CorePermissions.COMMAND_WHERE_AM_I)
    playerExecutor { player, _ ->
        WhereAmICommandHandler.send(player, player.uuid, player.username)
    }
}
