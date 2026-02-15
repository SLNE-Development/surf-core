package dev.slne.surf.core.core.common.redis.request

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

object SendPlayerToServerRequest {
    @Serializable
    data class Request(
        val player: SurfPlayer,
        val server: SurfServer,
        val requestId: @Contextual UUID
    ) : RedisRequest()

    @Serializable
    data object Acknowledged : RedisResponse()

    @Serializable
    data class Response(val requestId: @Contextual UUID, val status: SurfServerConnectResult) :
        RedisEvent()

    suspend fun createRequest(player: SurfPlayer, server: SurfServer, requestId: UUID) =
        redisApi.sendRequest<Acknowledged>(Request(player, server, requestId))
}