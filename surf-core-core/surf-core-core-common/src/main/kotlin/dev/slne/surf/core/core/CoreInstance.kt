package dev.slne.surf.core.core

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.rabbitmq.api.RabbitMQApi
import dev.slne.surf.redis.RedisApi

private val instance = requiredService<CoreInstance>()

interface CoreInstance {
    val redisApi: RedisApi
    val rabbitApi: RabbitMQApi

    companion object : CoreInstance by instance {
        val INSTANCE get() = instance
    }
}