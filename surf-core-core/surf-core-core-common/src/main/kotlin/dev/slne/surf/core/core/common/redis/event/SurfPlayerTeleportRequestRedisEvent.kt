package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
data class SurfPlayerTeleportRequestRedisEvent(
    val player: SurfPlayer,
    val target: SurfPlayer
) : RedisEvent()
