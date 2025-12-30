package dev.slne.surf.core.api.common.event.player

import dev.slne.surf.core.api.common.player.SurfPlayer

data class SurfPlayerConnectEvent(
    override val player: SurfPlayer
) : SurfPlayerEvent
