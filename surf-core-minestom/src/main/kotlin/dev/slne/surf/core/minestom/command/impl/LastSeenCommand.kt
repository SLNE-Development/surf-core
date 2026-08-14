package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutorSuspend
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.core.core.common.command.LastSeenCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import kotlinx.coroutines.Deferred

fun lastSeenCommand() = commandTree("lastseen") {
    withPermission(CorePermissions.COMMAND_LAST_SEEN)
    surfOfflinePlayerArgument("player") {
        anyExecutorSuspend { executor, args ->
            val player: Deferred<SurfPlayer?> by args
            LastSeenCommandHandler.send(executor, player.await())
        }
    }
}
