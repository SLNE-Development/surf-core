package dev.slne.surf.core.core.common.redis.request

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Duration.Companion.seconds

object SendPlayerToProxyRequest {
    @Serializable
    data class Request(
        val player: SurfPlayer,
        val server: SurfProxyServer,
        val requestId: @Contextual UUID
    ) : RedisRequest()

    @Serializable
    data object Acknowledged : RedisResponse()

    @Serializable
    data class Response(
        val playerUuid: @Contextual UUID,
        val status: SurfProxyServerConnectionResult
    ) : RedisEvent()

    suspend fun createRequest(player: SurfPlayer, server: SurfProxyServer, requestId: UUID) =
        redisApi.sendRequest<Acknowledged>(
            Request(player, server, requestId),
            10.seconds.inWholeMilliseconds
        )
}