package dev.slne.surf.core.velocity.redis.handler

import com.github.shynixn.mccoroutine.velocity.launch
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.redis.request.HandleRedisRequest
import dev.slne.surf.redis.request.RequestContext
import kotlin.jvm.optionals.getOrNull

object SendPlayerToProxyHandler {
    @HandleRedisRequest
    fun handlePlayerSendRequest(context: RequestContext<SendPlayerToProxyRequest.Request>) {
        val request = context.request
        val player = plugin.proxy.getPlayer(request.player.uuid).getOrNull() ?: return
        val proxy = request.server

        plugin.pluginContainer.launch {
            context.respond(SendPlayerToProxyRequest.Acknowledged).await()
            player.transferToHost(proxy.address)
        }
    }
}
