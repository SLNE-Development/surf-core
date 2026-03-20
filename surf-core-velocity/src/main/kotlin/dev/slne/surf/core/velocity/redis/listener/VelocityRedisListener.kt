package dev.slne.surf.core.velocity.redis.listener

import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.event.SurfPlayerResyncRedisEvent
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.task.surfPlayerSyncTask
import dev.slne.surf.redis.event.OnRedisEvent
import kotlin.jvm.optionals.getOrNull

object VelocityRedisListener {
    @OnRedisEvent
    fun onSurfPlayerMessage(event: SurfPlayerMessageRedisEvent) {
        plugin.proxy.getPlayer(event.uuid).getOrNull()?.sendMessage(event.message)
    }

    @OnRedisEvent
    fun onSurfPlayerResync(event: SurfPlayerResyncRedisEvent) {
        surfPlayerSyncTask.syncPlayers()
    }
}