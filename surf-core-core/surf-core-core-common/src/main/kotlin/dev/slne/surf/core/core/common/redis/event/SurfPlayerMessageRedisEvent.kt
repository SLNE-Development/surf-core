package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.redis.event.RedisEvent
import net.kyori.adventure.text.Component
import java.util.*

data class SurfPlayerMessageRedisEvent(
    val uuid: UUID,
    val message: Component
) : RedisEvent()
