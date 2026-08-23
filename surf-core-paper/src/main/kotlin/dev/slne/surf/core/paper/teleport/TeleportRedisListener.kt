package dev.slne.surf.core.paper.teleport

import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.core.common.command.NetworkTeleportCommandHandler
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.redis.event.OnRedisEvent

object TeleportRedisListener {
    @OnRedisEvent
    fun onTeleportRequest(event: SurfPlayerTeleportRequestRedisEvent) {
        val bukkitPlayer = event.player.bukkitPlayer ?: return
        val targetBukkitPlayer = event.target.bukkitPlayer ?: return

        targetBukkitPlayer.scheduler.run(plugin, {
            val destination = targetBukkitPlayer.location
            val targetName = targetBukkitPlayer.name

            bukkitPlayer.scheduler.run(plugin, {
                bukkitPlayer.teleportAsync(destination).thenRun {
                    NetworkTeleportCommandHandler.sendSuccess(bukkitPlayer, targetName)
                }
            }, null)
        }, null)
    }
}
