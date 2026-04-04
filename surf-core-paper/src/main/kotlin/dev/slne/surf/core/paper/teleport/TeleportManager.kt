package dev.slne.surf.core.paper.teleport

import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import org.bukkit.entity.Player

object TeleportManager {
    suspend fun teleport(player: Player, target: SurfPlayer) {
        val targetServer =
            target.currentServer ?: return
        val surfPlayer = player.surfPlayer

        if (player.surfPlayer.currentServer != targetServer) {
            if (surfPlayer.sendAwaiting(targetServer).isSuccessful()) {
                CoreInstance.redisApi.publishEvent(
                    SurfPlayerTeleportRequestRedisEvent(
                        surfPlayer,
                        target
                    )
                )
            } else {
                player.sendText {
                    appendErrorPrefix()
                    error("Du konntest nicht zum Zielserver teleportiert werden.")
                }
            }
            return
        }

        val bukkitTarget = target.bukkitPlayer ?: return

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