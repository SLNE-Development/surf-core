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
                        SendPlayerToServerRequest.Response(
                            SurfServerConnectResult(
                                SurfServerConnectResult.Status.SERVER_NOT_FOUND,
                                null
                            )
                        )
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
            ConnectionRequestBuilder.Status.ALREADY_CONNECTED -> SurfServerConnectResult(
                SurfServerConnectResult.Status.ALREADY_CONNECTED,
                this.reasonComponent.getOrNull()
            )

            ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS -> SurfServerConnectResult(
                SurfServerConnectResult.Status.CONNECTION_IN_PROGRESS,
                this.reasonComponent.getOrNull()
            )

            ConnectionRequestBuilder.Status.CONNECTION_CANCELLED -> SurfServerConnectResult(
                SurfServerConnectResult.Status.CONNECTION_CANCELLED,
                this.reasonComponent.getOrNull()
            )

            ConnectionRequestBuilder.Status.SERVER_DISCONNECTED -> SurfServerConnectResult(
                SurfServerConnectResult.Status.SERVER_DISCONNECTED,
                this.reasonComponent.getOrNull()
            )

            ConnectionRequestBuilder.Status.SUCCESS -> SurfServerConnectResult(
                SurfServerConnectResult.Status.SUCCESS,
                null
            )
        }
}