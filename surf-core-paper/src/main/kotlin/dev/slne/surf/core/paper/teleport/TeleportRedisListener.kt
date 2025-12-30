package dev.slne.surf.core.paper.teleport

import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

object TeleportRedisListener {
    @OnRedisEvent
    fun onTeleportRequest(event: SurfPlayerTeleportRequestRedisEvent) {
        val bukkitPlayer = event.player.bukkitPlayer ?: return
        val targetBukkitPlayer = event.target.bukkitPlayer ?: return

        bukkitPlayer.teleportAsync(targetBukkitPlayer.location).thenRun {
            bukkitPlayer.sendText {
                appendPrefix()
                success("Du wurdest zu ")
                variableValue(targetBukkitPlayer.name)
                success(" teleportiert.")
            }
        }
    }
}