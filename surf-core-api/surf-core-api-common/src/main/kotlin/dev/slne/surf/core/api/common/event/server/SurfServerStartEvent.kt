package dev.slne.surf.core.api.common.event.server

import dev.slne.surf.core.api.common.event.SurfEvent
import kotlinx.serialization.Serializable

@Serializable
data class SurfServerStartEvent(
    val serverName: String
) : SurfEvent
