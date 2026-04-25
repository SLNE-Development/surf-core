package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.request.ExecuteCommandServerRequest
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext

object ExecuteCommandRedisListener {
    @HandleRedisRequest
    suspend fun handleExecuteCommandRequest(context: RequestContext<ExecuteCommandServerRequest.Request>) {
        context.respond(
            ExecuteCommandServerRequest.Response(
                ExecuteCommandServerListener.executeCommand(
                    context.request.commonSurfServer,
                    context.request.command
                )
            )
        )
    }
}