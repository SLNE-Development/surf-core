package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutor
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.greedyStringArgument
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.core.core.common.command.NetworkBroadcastCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import net.kyori.adventure.text.minimessage.ParsingException

fun networkBroadcastCommand() = commandTree("nbroadcast") {
    withPermission(CorePermissions.COMMAND_NETWORK_BROADCAST)
    greedyStringArgument("message") { // TODO: port mini message arg in surf-api
        anyExecutor { executor, args ->
            val message: String by args

            try {
                NetworkBroadcastCommandHandler.broadcast(executor, message)
            } catch (e: ParsingException) {
                executor.sendMessage(text(e.message ?: "Parsing error", Colors.ERROR))
            }
        }
    }
}
