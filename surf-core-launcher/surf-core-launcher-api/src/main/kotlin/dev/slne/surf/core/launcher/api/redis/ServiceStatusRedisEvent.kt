package dev.slne.surf.core.launcher.api.redis

import dev.slne.surf.core.api.common.server.state.SurfServiceStatus
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class ServiceStatusRedisEvent(
    val serviceName: String,
    val status: SurfServiceStatus
) : RedisEvent()
