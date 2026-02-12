package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkServerMaxPlayersCommand() = commandTree("nmaxplayers") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER_MAX_PLAYERS)

    surfServerArgument("")
}