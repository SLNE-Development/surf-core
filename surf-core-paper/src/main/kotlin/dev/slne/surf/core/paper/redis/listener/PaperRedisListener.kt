package dev.slne.surf.core.paper.redis.listener

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.event.SurfServerChangeMaxPlayersRedisEvent
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.redis.event.OnRedisEvent
import org.bukkit.Bukkit

object PaperRedisListener {
    @OnRedisEvent
    fun onMaxPlayerChange(event: SurfServerChangeMaxPlayersRedisEvent) {
        if (event.server.name == SurfServer.current().name) {
            Bukkit.setMaxPlayers(event.maxPlayers)

            surfServerService.addServer(
                SurfServer.current().copy(
                    maxPlayers = event.maxPlayers
                )
            )
        }
    }
}