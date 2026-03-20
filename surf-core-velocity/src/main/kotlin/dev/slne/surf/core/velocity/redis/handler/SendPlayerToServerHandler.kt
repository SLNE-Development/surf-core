package dev.slne.surf.core.velocity.redis.handler

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.ConnectionRequestBuilder
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.redis.request.SendPlayerToServerRequest
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlinx.coroutines.future.await
import kotlin.jvm.optionals.getOrNull

object SendPlayerToServerHandler {
    @HandleRedisRequest
    fun handlePlayerSendRequest(context: RequestContext<SendPlayerToServerRequest.Request>) {
        val request = context.request
        val player = plugin.proxy.getPlayer(request.player.uuid).getOrNull() ?: return

        plugin.pluginContainer.launch {
            context.respond(SendPlayerToServerRequest.Acknowledged).await()

            val server = plugin.proxy.getServer(request.server.name).getOrNull()
            if (server == null) {
                CoreInstance.redisApi.publishEvent(
                    SendPlayerToServerRequest.Response(
                        request.requestId,
                        SurfServerConnectResult(
                            SurfServerConnectResult.Status.SERVER_NOT_FOUND,
                            null
                        )
                    )
                ).await()
            } else {
                CoreInstance.redisApi.publishEvent(
                    SendPlayerToServerRequest.Response(
                        request.requestId,
                        player.createConnectionRequest(server)
                            .connect()
                            .await()
                            .convertResult()
                    )
                ).await()
            }
        }
    }

}

fun ConnectionRequestBuilder.Result.convertResult(): SurfServerConnectResult {
    val resultStatus = when (this.status) {
        ConnectionRequestBuilder.Status.ALREADY_CONNECTED -> SurfServerConnectResult.Status.ALREADY_CONNECTED
        ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS -> SurfServerConnectResult.Status.CONNECTION_IN_PROGRESS
        ConnectionRequestBuilder.Status.CONNECTION_CANCELLED -> SurfServerConnectResult.Status.CONNECTION_CANCELLED
        ConnectionRequestBuilder.Status.SERVER_DISCONNECTED -> SurfServerConnectResult.Status.SERVER_DISCONNECTED
        ConnectionRequestBuilder.Status.SUCCESS -> SurfServerConnectResult.Status.SUCCESS
    }

    return SurfServerConnectResult(resultStatus, this.reasonComponent.getOrNull())
}
