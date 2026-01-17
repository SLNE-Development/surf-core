package dev.slne.surf.core.paper.teleport

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds

val teleportManager = TeleportManager()

class TeleportManager {
    suspend fun teleport(player: Player, target: SurfPlayer) {
        val targetServer =
            target.currentServer ?: error("Target player is not online on any server.")
        val surfPlayer = player.surfPlayer

        if (player.surfPlayer.currentServer != targetServer) {
            surfBukkitApi.sendPlayerToServer(player, targetServer.name)
            delay(1.seconds)
            redisApi.publishEvent(SurfPlayerTeleportRequestRedisEvent(surfPlayer, target))
            return
        }

        val bukkitTarget =
            target.bukkitPlayer ?: error("Target player is not online on this server.")

        player.teleportAsync(bukkitTarget.location).thenRun {
            player.sendText {
                appendPrefix()
                success("Du wurdest zu ")
                variableValue(bukkitTarget.name)
                success(" teleportiert.")
            }
        }
    }
}