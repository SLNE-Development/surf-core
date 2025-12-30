package dev.slne.surf.core.api.common.event

import kotlinx.serialization.Serializable

@Serializable
data class SurfServerStartEvent(
    val serverName: String
) : SurfEvent
