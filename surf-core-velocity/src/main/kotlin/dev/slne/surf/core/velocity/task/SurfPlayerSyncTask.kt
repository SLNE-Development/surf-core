package dev.slne.surf.core.velocity.task

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.proxy
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

    private fun syncPlayers() {
        plugin.pluginContainer.launch {
            val onlinePlayers = proxy.allPlayers.map { velocityPlayer ->
                val surfPlayer = surfPlayerService.findPlayerByUuid(velocityPlayer.uniqueId)
                    ?: surfPlayerService.getOrLoadOrCreatePlayerByUuid(velocityPlayer.uniqueId)
                        .also {
                            it.currentProxy = SurfProxyServer.current()
                        }
                val currentServerName = velocityPlayer.currentServer.getOrNull()?.serverInfo?.name
                val currentServer = currentServerName?.let { SurfServer[it] }
                surfPlayer.copy(currentServer = currentServer)
            }

            val onlineUuids = onlinePlayers.map { it.uuid }.toHashSet()

            SurfProxyServer.current().getPlayers()
                .filter { it.uuid !in onlineUuids }
                .forEach {
                    surfPlayerService.invalidatePlayer(it.uuid)
                    plugin.logger.info("Found invalid player ${it.uuid} (${it.username}), invalidating cache")
                }

            onlinePlayers.forEach { surfPlayerService.cachePlayer(it) }
        }
    }
}
