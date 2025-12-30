package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class SurfEventFireRedisEvent(
    val event: SurfEvent
) : RedisEvent()