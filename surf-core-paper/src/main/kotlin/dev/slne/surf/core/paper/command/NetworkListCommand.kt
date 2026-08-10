package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.core.core.common.command.NetworkListCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkListCommand() = commandTree("nlist") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_LIST)
    anyExecutor { executor, _ ->
        NetworkListCommandHandler.send(executor)
    }
}

