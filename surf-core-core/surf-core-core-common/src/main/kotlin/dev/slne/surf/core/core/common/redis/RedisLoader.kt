package dev.slne.surf.core.core.common.redis

import dev.slne.surf.core.core.common.event.LocalSurfEventBusListener
import dev.slne.surf.redis.RedisApi

val redisLoader = RedisLoader()
val redisApi get() = redisLoader.redisApi

class RedisLoader {
    val redisApi = RedisApi.create()

    fun load() {
    }

    fun connect(vararg extraListeners: Any) {
        redisApi.subscribeToEvents(LocalSurfEventBusListener)
        extraListeners.forEach { redisApi.subscribeToEvents(it) }

        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}