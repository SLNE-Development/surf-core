package dev.slne.surf.core.core.common.redis

import dev.slne.surf.core.core.common.event.LocalSurfEventBusListener
import dev.slne.surf.redis.RedisApi

val redisLoader = RedisLoader()
val redisApi get() = redisLoader.redisApi

class RedisLoader {
    lateinit var redisApi: RedisApi

    fun load() {
        redisApi = RedisApi.create()
    }

    fun connect() {
        redisApi.subscribeToEvents(LocalSurfEventBusListener)
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}