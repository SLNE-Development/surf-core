package dev.slne.surf.core.velocity.redis.handler

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.ConnectionRequestBuilder
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.common.redis.SendPlayerToServerRequest
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.future.await
import kotlin.jvm.optionals.getOrNull

object VelocityRedisResponseHandler {
    @HandleRedisRequest
    fun handlePlayerSendRequest(context: RequestContext<SendPlayerToServerRequest.Request>) {
        plugin.pluginContainer.launch {
            val player =
                plugin.proxy.getPlayer(context.request.player.uuid).getOrNull() ?: return@launch
            val server =
                plugin.proxy.getServer(context.request.server.name).getOrNull() ?: run {
                    context.respond(
                        SendPlayerToServerRequest.Response(SurfServerConnectResult.SERVER_NOT_FOUND)
                    )
                    return@launch
                }

            context.respond(
                SendPlayerToServerRequest.Response(
                    player.createConnectionRequest(server).connect().await().toLocal()
                )
            )
        }
    }

    private fun ConnectionRequestBuilder.Result.toLocal(): SurfServerConnectResult =
        when (this.status) {
            ConnectionRequestBuilder.Status.ALREADY_CONNECTED -> SurfServerConnectResult.ALREADY_CONNECTED
            ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS -> SurfServerConnectResult.CONNECTION_IN_PROGRESS
            ConnectionRequestBuilder.Status.CONNECTION_CANCELLED -> SurfServerConnectResult.CONNECTION_CANCELLED
            ConnectionRequestBuilder.Status.SERVER_DISCONNECTED -> SurfServerConnectResult.SERVER_DISCONNECTED
            ConnectionRequestBuilder.Status.SUCCESS -> SurfServerConnectResult.SUCCESS
        }
}