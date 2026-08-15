package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutor
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.surf.core.core.common.command.NetworkListCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun networkListCommand() = commandTree("nlist") {
    withPermission(CorePermissions.COMMAND_NETWORK_LIST)
    anyExecutor { executor, _ ->
        NetworkListCommandHandler.send(executor)
    }
}
