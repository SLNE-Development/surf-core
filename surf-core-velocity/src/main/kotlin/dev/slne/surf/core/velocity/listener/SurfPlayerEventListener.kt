package dev.slne.surf.core.velocity.listener

import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.player.surfPlayerService

object SurfPlayerEventListener {
    @SurfEventHandler
    fun onPlayerConnect(event: SurfPlayerConnectEvent) {
        surfPlayerService.cachePlayer(event.player)
    }

    @SurfEventHandler
    fun onPlayerDisconnect(event: SurfPlayerDisconnectEvent) {
        surfPlayerService.invalidatePlayer(event.player.uuid)
    }
}