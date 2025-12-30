package dev.slne.surf.core.core.common.event

import dev.slne.surf.core.core.common.redis.event.SurfEventFireRedisEvent
import dev.slne.surf.redis.event.OnRedisEvent

object LocalSurfEventBusListener {
    @OnRedisEvent
    fun onSurfEventFire(event: SurfEventFireRedisEvent) {
        surfEventBus.fireLocal(event.event)
    }
}