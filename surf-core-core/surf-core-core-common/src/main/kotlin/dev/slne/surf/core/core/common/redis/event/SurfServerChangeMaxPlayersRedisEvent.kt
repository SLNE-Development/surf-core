package dev.slne.surf.core.core.common.redis.event

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 *
 * Requests a change of the maximum player count for a server.
 * Note: This event is only available for backend servers, not for proxy servers.
 *
 * @see SurfServer
 */
@Serializable
data class SurfServerChangeMaxPlayersRedisEvent(
    val server: SurfServer,
    val maxPlayers: Int
) : RedisEvent()
