package dev.slne.surf.core.minestom

import dev.slne.surf.core.api.minestom.util.minestomPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.redis.event.OnRedisEvent

object MinestomTeleportRedisListener {
    @OnRedisEvent
    fun onTeleportRequest(event: SurfPlayerTeleportRequestRedisEvent) {
        val player = event.player.minestomPlayer ?: return
        val target = event.target.minestomPlayer ?: return

        player.scheduleNextTick {
            player.teleport(target.position).thenRun {
                NetworkTeleportCommandHandler.sendSuccess(player, target.username)
            }
        }
    }
}
