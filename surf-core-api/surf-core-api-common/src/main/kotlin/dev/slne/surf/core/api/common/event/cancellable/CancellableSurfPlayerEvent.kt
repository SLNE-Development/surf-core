package dev.slne.surf.core.api.common.event.cancellable

import dev.slne.surf.core.api.common.player.SurfPlayer

interface CancellableSurfPlayerEvent : CancellableSurfEvent {
    val player: SurfPlayer
}