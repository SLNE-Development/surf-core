package dev.slne.surf.core.core

import dev.slne.surf.rabbitmq.api.RabbitMQApi
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.surfapi.core.api.util.requiredService

private val instance = requiredService<CoreInstance>()

interface CoreInstance {
    val redisApi: RedisApi
    val rabbitApi: RabbitMQApi

    companion object : CoreInstance by instance {
        val INSTANCE get() = instance
    }
}