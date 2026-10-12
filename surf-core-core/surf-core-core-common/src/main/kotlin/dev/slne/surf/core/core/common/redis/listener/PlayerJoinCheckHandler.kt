package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.join.CoreJoinListenerRegistry
import dev.slne.surf.core.core.common.redis.request.PlayerJoinCheckRequest
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.launch

object PlayerJoinCheckHandler {
    @HandleRedisRequest
    fun handleJoinCheckRequest(context: RequestContext<PlayerJoinCheckRequest.Request>) {
        val request = context.request
        if (request.targetServerName != SurfCoreApi.getCurrentServerName()) return

        context.launch {
            context.respond(PlayerJoinCheckRequest.Acknowledged).await()

            val result = CoreJoinListenerRegistry.check(request.playerUuid)
            CoreInstance.redisApi.publishEvent(
                PlayerJoinCheckRequest.Response(request.requestId, result)
            ).await()
        }
    }
}
