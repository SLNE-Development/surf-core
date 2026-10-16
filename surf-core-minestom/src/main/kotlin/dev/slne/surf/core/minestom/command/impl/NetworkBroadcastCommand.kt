package dev.slne.surf.core.minestom.command.impl

import dev.slne.surf.api.minestom.command.args.miniMessageArgument
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.core.core.common.command.NetworkBroadcastCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import net.kyori.adventure.text.Component

fun networkBroadcastCommand() = commandTree("nbroadcast") {
    withPermission(CorePermissions.COMMAND_NETWORK_BROADCAST)
    miniMessageArgument("message", greedy = true) {
        anyExecutor { executor, args ->
            val message: Component by args
            NetworkBroadcastCommandHandler.broadcast(executor, message)
        }
    }
}
