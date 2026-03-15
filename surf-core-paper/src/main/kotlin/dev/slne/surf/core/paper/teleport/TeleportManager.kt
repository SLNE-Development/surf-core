package dev.slne.surf.core.paper.teleport

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.entity.Player

val teleportManager = TeleportManager()

class TeleportManager {
    suspend fun teleport(player: Player, target: SurfPlayer) {
        val targetServer =
            target.currentServer ?: return
        val surfPlayer = player.surfPlayer

        if (player.surfPlayer.currentServer != targetServer) {
            surfPlayer.sendAwaiting(targetServer)
            redisApi.publishEvent(SurfPlayerTeleportRequestRedisEvent(surfPlayer, target))
            return
        }

        val bukkitTarget =
            target.bukkitPlayer ?: return

        player.teleportAsync(bukkitTarget.location).thenRun {
            player.sendText {
                appendSuccessPrefix()
                success("Du wurdest zu ")
                variableValue(bukkitTarget.name)
                success(" teleportiert.")
            }
        }
    }
}