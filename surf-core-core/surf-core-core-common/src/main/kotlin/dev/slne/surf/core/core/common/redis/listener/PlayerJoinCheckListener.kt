package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.core.core.common.redis.request.PlayerJoinCheckRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerJoinCheckResultWatcher
import dev.slne.surf.redis.event.OnRedisEvent

object PlayerJoinCheckListener {
    @OnRedisEvent
    fun onPlayerJoinCheckResponse(response: PlayerJoinCheckRequest.Response) {
        PlayerJoinCheckResultWatcher.complete(response.requestId, response.result)
    }
}
