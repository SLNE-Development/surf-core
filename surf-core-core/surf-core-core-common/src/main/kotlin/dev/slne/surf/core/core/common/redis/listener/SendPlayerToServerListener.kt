package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.request.SendPlayerToServerRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerServerConnectionResultWatcher
import dev.slne.surf.redis.event.OnRedisEvent

object SendPlayerToServerListener {

    @OnRedisEvent
    fun onSendPlayerToServerResponse(response: SendPlayerToServerRequest.Response) {
        PlayerServerConnectionResultWatcher.complete(response.requestId, response.status)
    }
}