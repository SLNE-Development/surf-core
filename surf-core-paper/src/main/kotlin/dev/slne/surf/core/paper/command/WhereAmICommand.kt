package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun whereAmICommand() = commandTree("whereami") {
    withPermission(PermissionRegistry.COMMAND_WHERE_AM_I)
    playerExecutor { player, _ ->
        WhereAmICommandHandler.send(player, player.uniqueId, player.name)
    }
}
