package dev.slne.surf.core.core.common.redis.request

import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

object ExecuteCommandServerRequest {
    @Serializable
    data class Request(
        val commonSurfServer: CommonSurfServer,
        val command: String
    ) : RedisRequest()

    @Serializable
    data class Response(
        val status: Boolean
    ) : RedisResponse()

    suspend fun createRequest(commonSurfServer: CommonSurfServer, command: String) =
        runCatching {
            CoreInstance.redisApi.sendRequest<Response>(
                Request(commonSurfServer, command),
                10.seconds.inWholeMilliseconds
            )
        }.getOrNull() ?: Response(false)
}