package dev.slne.surf.core.velocity.redis.listener

import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.velocity.plugin
import kotlin.jvm.optionals.getOrNull

object VelocitySurfPlayerRedisListener {
    @OnRedisEvent
    fun onSurfPlayerMessage(event: SurfPlayerMessageRedisEvent) {
        plugin.proxy.getPlayer(event.uuid).getOrNull()?.sendMessage(event.message)
    }
}