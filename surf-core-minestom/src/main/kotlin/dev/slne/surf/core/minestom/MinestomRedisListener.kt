package dev.slne.surf.core.minestom

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.event.SurfServerChangeMaxPlayersRedisEvent
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.redis.event.OnRedisEvent

object MinestomRedisListener {
    @OnRedisEvent
    fun onMaxPlayerChange(event: SurfServerChangeMaxPlayersRedisEvent) {
        if (event.server.name != SurfServer.current().name) return

        SurfServerService.addServer(
            SurfServer.current().copy(maxPlayers = event.maxPlayers)
        )
    }
}
