package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.redis.event.OnRedisEvent

object SendPlayerToProxyListener {

    @OnRedisEvent
    fun onSendPlayerToProxyResponse(response: SendPlayerToProxyRequest.Response) {
        PlayerProxyConnectionResultWatcher.complete(response.playerUuid, response.status)
    }
}