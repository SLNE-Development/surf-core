package dev.slne.surf.core.paper.teleport

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import dev.slne.surf.core.paper.plugin
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

object TeleportManager {
    suspend fun teleport(player: Player, target: SurfPlayer) {
        val surfPlayer = player.surfPlayer
        NetworkTeleportCommandHandler.teleport(player, surfPlayer, target) {
            val bukkitTarget = target.bukkitPlayer ?: return@teleport false
            val destination = withContext(plugin.entityDispatcher(bukkitTarget)) {
                bukkitTarget.location
            }

            withContext(plugin.entityDispatcher(player)) {
                player.teleportAsync(destination)
            }.await().also { success ->
                if (success) NetworkTeleportCommandHandler.sendSuccess(player, bukkitTarget.name)
            }
        }
    }
}
