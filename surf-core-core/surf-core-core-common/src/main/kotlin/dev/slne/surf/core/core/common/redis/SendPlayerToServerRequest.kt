package dev.slne.surf.core.core.common.redis

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Serializable

object SendPlayerToServerRequest {
    @Serializable
    data class Request(val player: SurfPlayer, val server: CommonSurfServer) : RedisRequest()

    @Serializable
    data class Response(val status: SurfServerConnectResult) : RedisResponse()

    suspend fun sendPlayerToServer(player: SurfPlayer, server: CommonSurfServer) =
        redisApi.sendRequest<Response>(Request(player, server)).status
}