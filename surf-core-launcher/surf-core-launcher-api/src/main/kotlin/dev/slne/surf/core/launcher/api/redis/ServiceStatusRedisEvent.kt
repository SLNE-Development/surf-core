package dev.slne.surf.core.launcher.api.redis

import dev.slne.surf.core.api.common.server.state.SurfServiceStatus
import kotlinx.serialization.Serializable

@Serializable
data class ServiceStatusRedisEvent(
    val serviceName: String,
    val status: SurfServiceStatus
)
