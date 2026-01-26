package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.core.paper.permission.PermissionRegistry

fun networkSendCommand() = commandTree("nsend") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SEND)
}