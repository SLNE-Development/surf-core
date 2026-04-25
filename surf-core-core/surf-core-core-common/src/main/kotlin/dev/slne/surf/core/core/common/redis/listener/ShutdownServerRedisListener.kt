package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.request.ShutdownServerRequest
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext

object ShutdownServerRedisListener {
    @HandleRedisRequest
    fun handleShutdownRequest(context: RequestContext<ShutdownServerRequest.Request>) {
        ShutdownServerListener.shutdown(
            context.request.commonSurfServer,
            context.request.reason
        )?.let {
            context.respond(
                ShutdownServerRequest.Response(
                    it
                )
            )
        }

    }
}