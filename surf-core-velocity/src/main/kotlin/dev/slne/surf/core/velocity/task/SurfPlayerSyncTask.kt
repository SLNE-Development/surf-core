package dev.slne.surf.core.velocity.task

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.proxy
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

val surfPlayerSyncTask = SurfPlayerSyncTask()

class SurfPlayerSyncTask {
    private var task: ScheduledTask? = null

    fun start() {
        task = proxy.scheduler
            .buildTask(plugin, Runnable { syncPlayers() })
            .delay(1L, TimeUnit.MINUTES)
            .repeat(1L, TimeUnit.MINUTES)
            .schedule()
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    fun syncPlayers() {
        plugin.pluginContainer.launch {
            val currentProxy = SurfProxyServer.current()
            val currentProxyName = currentProxy.name
            val velocityPlayers = proxy.allPlayers

            val onlinePlayers = ObjectArrayList<SurfPlayer>(velocityPlayers.size)
            val onlineUuids = ObjectOpenHashSet<UUID>(velocityPlayers.size)

            for (velocityPlayer in velocityPlayers) {
                val uuid = velocityPlayer.uniqueId
                val cached = SurfPlayerService.findPlayerByUuid(uuid)
                    ?: SurfPlayerService.getOrLoadOrCreatePlayerByUuid(uuid)
                        .copy(currentProxyName = currentProxyName)

                val velocityServerName = velocityPlayer.currentServer.getOrNull()?.serverInfo?.name

                onlinePlayers += if (velocityServerName != null) {
                    cached.copy(currentServerName = velocityServerName)
                } else {
                    cached
                }
                onlineUuids += uuid
            }

            for (cachedPlayer in currentProxy.getPlayers()) {
                if (cachedPlayer.uuid in onlineUuids) {
                    continue
                }

                SurfPlayerService.invalidatePlayer(cachedPlayer.uuid)
                plugin.logger.info("Found invalid player ${cachedPlayer.uuid} (${cachedPlayer.username}), invalidating cache")
            }

            for (player in onlinePlayers) {
                if (SurfPlayerService.findPlayerByUuid(player.uuid) == player) {
                    continue
                }

                SurfPlayerService.cachePlayer(player)
            }
        }
    }
}
