package dev.slne.surf.core.velocity.redis.listener

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.core.api.common.server.state.SurfServiceStatus
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.event.SurfPlayerResyncRedisEvent
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.launcher.api.redis.ServiceStatusRedisEvent
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.task.surfPlayerSyncTask
import dev.slne.surf.redis.event.OnRedisEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

object VelocityRedisListener {
    @OnRedisEvent
    fun onSurfPlayerMessage(event: SurfPlayerMessageRedisEvent) {
        plugin.proxy.getPlayer(event.uuid).getOrNull()?.sendMessage(event.message)
    }

    @OnRedisEvent
    fun onSurfPlayerResync(event: SurfPlayerResyncRedisEvent) {
        surfPlayerSyncTask.syncPlayers()
    }

    private val instableConnectionCache = Caffeine.newBuilder()
        .expireAfterWrite(10.seconds)
        .build<Int, ServiceStatusRedisEvent>()

    val ignoringPlayers: ConcurrentHashMap.KeySetView<UUID, Boolean> = ConcurrentHashMap.newKeySet()

    @OnRedisEvent
    fun onServiceStatus(event: ServiceStatusRedisEvent) {
        val status = event.status
        val cachedAmount =
            instableConnectionCache.asMap().values.filter { it.status == status }.size

        if (cachedAmount < 2) {
            plugin.proxy.allPlayers.filter { it.hasPermission("surf.core.servernotify") }.forEach {
                if (ignoringPlayers.contains(it.uniqueId)) {
                    return@forEach
                }

                it.sendText {
                    appendCorePrefix()
                    info("Der Server ")
                    variableValue(event.serviceName)

                    when (status) {
                        SurfServiceStatus.UNREACHABLE -> info("hat derzeit Verbindungsprobleme!")
                        SurfServiceStatus.CRASHED -> {
                            info("hat die Verbindung verloren! (CRASH?)")
                        }
                    }
                }
            }
        } else {
            plugin.proxy.allPlayers.filter { it.hasPermission("surf.core.servernotify") }.forEach {
                if (ignoringPlayers.contains(it.uniqueId)) {
                    return@forEach
                }

                it.sendText {
                    appendCorePrefix()
                    info("Der Server ")
                    variableValue(event.serviceName)
                    info(" hat derzeit Verbindungsprobleme! Check server logs. (x$cachedAmount $status)")
                }
            }
        }

        instableConnectionCache.put(random.nextInt(), event)
    }
}