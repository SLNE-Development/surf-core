package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.PlayerConnectionResultWatcher
import dev.slne.surf.core.core.common.redis.SendPlayerToServerRequest
import dev.slne.surf.redis.event.OnRedisEvent

object SendPlayerToServerListener {

    @OnRedisEvent
    fun onSendPlayerToServerResponse(response: SendPlayerToServerRequest.Response) {
        PlayerConnectionResultWatcher.complete(response.requestId, response.status)
    }
}