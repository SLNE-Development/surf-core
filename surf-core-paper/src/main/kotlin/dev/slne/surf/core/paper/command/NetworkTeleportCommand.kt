package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.core.paper.teleport.TeleportManager

fun networkTeleportCommand() = commandTree("ntp") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_TELEPORT)
    surfPlayerArgument("target") {
        playerExecutor { player, args ->
            val target: SurfPlayer by args

            plugin.launch {
                TeleportManager.teleport(player, target)
            }
        }
    }
}
