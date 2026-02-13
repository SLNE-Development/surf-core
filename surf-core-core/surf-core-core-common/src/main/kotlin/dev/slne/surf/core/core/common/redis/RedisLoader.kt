package dev.slne.surf.core.core.common.redis

import dev.slne.surf.core.core.common.event.LocalSurfEventBusListener
import dev.slne.surf.redis.RedisApi

val redisLoader = RedisLoader()
val redisApi get() = redisLoader.redisApi

class RedisLoader {
    val redisApi = RedisApi.create()

    fun load() {
    }

    fun withRequestResponseHandler(handler: Any) {
        redisApi.registerRequestHandler(handler)
    }

    fun withListener(listener: Any) {
        redisApi.subscribeToEvents(listener)
    }

    fun connect() {
        redisApi.subscribeToEvents(LocalSurfEventBusListener)
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}