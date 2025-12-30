package dev.slne.surf.core.api.common.event

import dev.slne.surf.core.api.common.player.SurfPlayer

interface SurfPlayerEvent : SurfEvent {
    val player: SurfPlayer
}