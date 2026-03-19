package dev.slne.surf.core.client

import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi

interface ClientCoreInstance : CoreInstance {
    val clientLoader: ClientLoader

    override val rabbitApi: ClientRabbitMQApi get() = clientLoader.rabbitApi
    override val redisApi: RedisApi get() = clientLoader.redisApi

    companion object : ClientCoreInstance by CoreInstance.INSTANCE as ClientCoreInstance {
        val INSTANCE get() = CoreInstance.INSTANCE as ClientCoreInstance
    }
}