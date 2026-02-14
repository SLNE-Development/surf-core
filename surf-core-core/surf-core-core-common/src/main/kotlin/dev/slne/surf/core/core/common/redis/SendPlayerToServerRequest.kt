package dev.slne.surf.core.core.common.redis

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

object SendPlayerToServerRequest {
    @Serializable
    data class Request(val player: SurfPlayer, val server: CommonSurfServer, val requestId: @Contextual UUID) : RedisRequest()

    @Serializable
    data object Acknowledged : RedisResponse()

    @Serializable
    data class Response(val requestId: @Contextual UUID, val status: SurfServerConnectResult) : RedisEvent()

    suspend fun createRequest(player: SurfPlayer, server: CommonSurfServer, requestId: UUID) =
        redisApi.sendRequest<Acknowledged>(Request(player, server, requestId))
}