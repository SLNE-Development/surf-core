package dev.slne.surf.core.velocity.task

import com.velocitypowered.api.scheduler.ScheduledTask
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.proxy
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

val surfPlayerSyncTask = SurfPlayerSyncTask()

class SurfPlayerSyncTask {
    private var task: ScheduledTask? = null

    fun start() {
        task = proxy.scheduler()
            .buildTask(plugin) { syncPlayers() }
            .delay(1L, TimeUnit.MINUTES)
            .repeat(1L, TimeUnit.MINUTES)
            .schedule()
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun syncPlayers() {
        val onlinePlayers = proxy.allPlayers.mapNotNull { velocityPlayer ->
            val surfPlayer = surfPlayerService.findPlayerByUuid(velocityPlayer.uniqueId) ?: return@mapNotNull null
            val currentServerName = velocityPlayer.currentServer.getOrNull()?.serverInfo?.name
            val currentServer = currentServerName?.let { CommonSurfServer[it] }
            surfPlayer.copy(currentServer = currentServer)
        }

        val onlineUuids = onlinePlayers.map { it.uuid }.toHashSet()

        // Only remove stale entries that belong to this proxy — do not touch players
        // cached by other proxy instances in a multiproxy setup.
        SurfProxyServer.current().getPlayers()
            .filter { it.uuid !in onlineUuids }
            .forEach { surfPlayerService.invalidatePlayer(it.uuid) }

        onlinePlayers.forEach { surfPlayerService.cachePlayer(it) }
    }
}
