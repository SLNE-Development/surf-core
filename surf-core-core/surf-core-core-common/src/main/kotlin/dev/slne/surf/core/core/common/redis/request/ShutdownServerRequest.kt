package dev.slne.surf.core.core.common.redis.request

import dev.slne.surf.api.core.serializer.adventure.component.SerializableComponent
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import kotlin.time.Duration.Companion.seconds

object ShutdownServerRequest {
    @Serializable
    data class Request(
        val commonSurfServer: CommonSurfServer,
        val reason: SerializableComponent?
    ) : RedisRequest()

    @Serializable
    data class Response(
        val status: Boolean
    ) : RedisResponse()

    suspend fun createRequest(commonSurfServer: CommonSurfServer, reason: Component?) =
        runCatching {
            CoreInstance.redisApi.sendRequest<Response>(
                Request(commonSurfServer, reason),
                10.seconds.inWholeMilliseconds
            )
        }.getOrNull() ?: Response(false)
}