package dev.slne.surf.core.api.common.event.player

import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.player.SurfPlayer

interface SurfPlayerEvent : SurfEvent {
    val player: SurfPlayer
}