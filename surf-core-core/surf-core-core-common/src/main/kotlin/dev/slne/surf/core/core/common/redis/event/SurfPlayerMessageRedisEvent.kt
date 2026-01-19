package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.surfapi.core.api.serializer.adventure.component.SerializableComponent
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SurfPlayerMessageRedisEvent(
    val uuid: @Contextual UUID,
    val message: SerializableComponent
) : RedisEvent()
