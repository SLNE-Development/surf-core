package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
object SurfPlayerResyncRedisEvent : RedisEvent()