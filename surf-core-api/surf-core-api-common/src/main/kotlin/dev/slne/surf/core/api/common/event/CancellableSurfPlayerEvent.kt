package dev.slne.surf.core.api.common.event

import dev.slne.surf.core.api.common.player.SurfPlayer
import kotlinx.serialization.Serializable

@Serializable
sealed interface CancellableSurfPlayerEvent : CancellableSurfEvent {
    val player: SurfPlayer
}