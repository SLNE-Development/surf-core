package dev.slne.surf.core.paper.teleport

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player

object TeleportManager {
    suspend fun teleport(player: Player, target: SurfPlayer) {
        val surfPlayer = player.surfPlayer
        NetworkTeleportCommandHandler.teleport(player, surfPlayer, target) {
            val bukkitTarget = target.bukkitPlayer ?: return@teleport false
            player.teleportAsync(bukkitTarget.location).await().also { success ->
                if (success) NetworkTeleportCommandHandler.sendSuccess(player, bukkitTarget.name)
            }
        }
    }
}
