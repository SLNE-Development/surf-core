package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.paper.command.args.MiniMessageArgument
import dev.slne.surf.core.core.common.command.NetworkBroadcastCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import net.kyori.adventure.text.Component

fun networkBroadcastCommand() = commandTree("nbroadcast") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_BROADCAST)
    argument(MiniMessageArgument("message")) {
        anyExecutor { executor, args ->
            val message: Component by args

            NetworkBroadcastCommandHandler.broadcast(executor, message)
        }
    }
}
