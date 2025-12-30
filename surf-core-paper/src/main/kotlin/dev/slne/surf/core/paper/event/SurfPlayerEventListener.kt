package dev.slne.surf.core.paper.event

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.plugin

object SurfPlayerEventListener {
    @SurfEventHandler
    fun onPlayerConnect(event: SurfPlayerConnectEvent) {
        plugin.launch {
            surfPlayerService.cachePlayer(event.player)
        }
    }

    @SurfEventHandler
    fun onPlayerDisconnect(event: SurfPlayerDisconnectEvent) {
        surfPlayerService.invalidatePlayer(event.player.uuid)
    }
}