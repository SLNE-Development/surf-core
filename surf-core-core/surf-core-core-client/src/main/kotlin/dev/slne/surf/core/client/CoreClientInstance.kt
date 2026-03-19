@file:Suppress("BlockingMethodInNonBlockingContext")

package dev.slne.surf.core.client

import dev.slne.surf.core.core.common.event.LocalSurfEventBusListener
import dev.slne.surf.core.core.common.redis.listener.SendPlayerToProxyListener
import dev.slne.surf.core.core.common.redis.listener.SendPlayerToServerListener
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi

object CoreClientInstance {
    private val redisApi = RedisApi.create("surf-core")
    private val rabbitApi = ClientRabbitMQApi.create(1, "surf-core")

    suspend fun onLoad() {
        // Rabbit
        rabbitApi.freezeAndConnect()

        // Redis
        redisApi.subscribeToEvents(LocalSurfEventBusListener)
        withListener(SendPlayerToServerListener)
        withListener(SendPlayerToProxyListener)
    }

    fun connectRedis() {
        redisApi.freezeAndConnect()
    }

    suspend fun onEnable() {
    }

    suspend fun onDisable() {
        redisApi.disconnect()
        rabbitApi.disconnect()
    }
    
    fun withRequestResponseHandler(handler: Any) {
        redisApi.registerRequestHandler(handler)
    }

    fun withListener(listener: Any) {
        redisApi.subscribeToEvents(listener)
    }
}