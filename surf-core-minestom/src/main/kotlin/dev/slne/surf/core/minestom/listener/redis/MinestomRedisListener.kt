package dev.slne.surf.core.minestom.listener.redis

import dev.slne.surf.api.minestom.player.PlayerLimit
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.event.SurfServerChangeMaxPlayersRedisEvent
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.redis.event.OnRedisEvent

object MinestomRedisListener {
    @OnRedisEvent
    fun onMaxPlayerChange(event: SurfServerChangeMaxPlayersRedisEvent) {
        if (event.server.name != SurfServer.current().name) {
            return
        }

        if (event.maxPlayers <= 0) {
            return
        }

        PlayerLimit.maxPlayers = event.maxPlayers

        SurfServerService.addServer(
            SurfServer.current().copy(maxPlayers = event.maxPlayers)
        )
    }
}
