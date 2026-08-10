package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.core.core.common.command.LastSeenCommandHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import kotlinx.coroutines.Deferred

fun lastSeenCommand() = commandTree("lastseen") {
    withPermission(PermissionRegistry.COMMAND_LAST_SEEN)
    surfOfflinePlayerArgument("player") {
        anyExecutor { executor, args ->
            val player: Deferred<SurfPlayer?> by args

            plugin.launch {
                LastSeenCommandHandler.send(executor, player.await())
            }
        }
    }
}
