package dev.slne.surf.core.api.common.event

import dev.slne.surf.core.api.common.player.SurfPlayer
import kotlinx.serialization.Serializable

@Serializable
data class SurfPlayerConnectEvent(
    override val player: SurfPlayer
) : SurfPlayerEvent
