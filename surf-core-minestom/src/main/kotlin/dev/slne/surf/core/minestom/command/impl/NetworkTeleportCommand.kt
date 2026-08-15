package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.minestom.util.minestomPlayer
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import kotlinx.coroutines.future.await

fun networkTeleportCommand() = commandTree("ntp") {
    withPermission(CorePermissions.COMMAND_NETWORK_TELEPORT)
    surfPlayerArgument("target") {
        playerExecutorSuspend { player, args ->
            val target: SurfPlayer by args
            NetworkTeleportCommandHandler.teleport(player, player.surfPlayer, target) {
                val targetPlayer = target.minestomPlayer ?: return@teleport false
                player.teleport(targetPlayer.position).await()
                NetworkTeleportCommandHandler.sendSuccess(player, targetPlayer.username)
                true
            }
        }
    }
}
