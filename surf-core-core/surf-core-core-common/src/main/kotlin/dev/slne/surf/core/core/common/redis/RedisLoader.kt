package dev.slne.surf.core.core.common.redis

import dev.slne.surf.redis.RedisApi
import java.nio.file.Path

val redisLoader = RedisLoader()
val redisApi get() = redisLoader.redisApi

class RedisLoader {
    lateinit var redisApi: RedisApi

    fun load(dataPath: Path) {
        redisApi = RedisApi.create(dataPath)
    }

    fun connect() {
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}