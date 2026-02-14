package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.permissionSurfServerArgument
import dev.slne.surf.core.api.paper.util.toSurfPlayer
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkServerCommand() = commandTree("nserver") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER)

    permissionSurfServerArgument("server") {
        playerExecutor { player, args ->
            val server: SurfServer by args
            val surfPlayer = player.toSurfPlayer()

            player.sendText {
                appendSuccessPrefix()
                success("Du wirst zum Server ")
                variableValue(server.name)
                success(" gesendet...")
            }

            surfPlayer.send(server)
        }
    }
}