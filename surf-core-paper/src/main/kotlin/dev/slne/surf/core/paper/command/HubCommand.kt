package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.command.HubCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun hubCommand() = commandTree("hub") {
    withAliases("lobby", "l")
    withPermission(PermissionRegistry.COMMAND_HUB)

    playerExecutor { player, _ ->
        plugin.launch {
            HubCommandHandler.sendToHub(player, player.surfPlayer)
        }
    }
}
