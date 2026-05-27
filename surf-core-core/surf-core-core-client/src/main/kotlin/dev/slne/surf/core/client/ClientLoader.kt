@file:Suppress("BlockingMethodInNonBlockingContext")

package dev.slne.surf.core.client

import dev.slne.surf.core.core.common.event.LocalSurfEventBusListener
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.listener.ExecuteCommandRedisListener
import dev.slne.surf.core.core.common.redis.listener.SendPlayerToProxyListener
import dev.slne.surf.core.core.common.redis.listener.SendPlayerToServerListener
import dev.slne.surf.core.core.common.redis.listener.ShutdownServerRedisListener
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi
import java.nio.file.Path

class ClientLoader(
    dataPath: Path
) {
    val redisApi = RedisApi.create("surf-core")
    val rabbitApi = ClientRabbitMQApi.create("surf-core", dataPath)

    suspend fun onBootstrap() {

    }

    @Suppress("UnusedExpression")
    suspend fun onLoad() {
        // Rabbit
        rabbitApi.freezeAndConnect()

        // Redis
        redisApi.subscribeToEvents(LocalSurfEventBusListener)
        withListener(SendPlayerToServerListener)
        withListener(SendPlayerToProxyListener)

        withRequestResponseHandler(ShutdownServerRedisListener)
        withRequestResponseHandler(ExecuteCommandRedisListener)

        // Initialize Redis Maps
        SurfPlayerService
        SurfServerService
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