package dev.slne.surf.core.core.common.redis.request

import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.redis.request.RedisRequest
import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Asks a target server or proxy whether a player could join it.
 *
 * 1. The requester sends a [Request] containing the player uuid.
 * 2. The target answers with [Acknowledged].
 * 3. The target runs all of its join listeners and publishes the combined result as [Response].
 */
object PlayerJoinCheckRequest {
    @Serializable
    data class Request(
        val playerUuid: @Contextual UUID,
        val targetServerName: String,
        val requestId: @Contextual UUID,
        val force: Boolean
    ) : RedisRequest()

    @Serializable
    data object Acknowledged : RedisResponse()

    @Serializable
    data class Response(
        val requestId: @Contextual UUID,
        val result: SurfJoinCheckResult
    ) : RedisEvent()

    suspend fun createRequest(
        playerUuid: UUID,
        targetServerName: String,
        requestId: UUID,
        force: Boolean
    ) = CoreInstance.redisApi.sendRequest<Acknowledged>(
        Request(playerUuid, targetServerName, requestId, force),
        5.seconds.inWholeMilliseconds
    )
}
