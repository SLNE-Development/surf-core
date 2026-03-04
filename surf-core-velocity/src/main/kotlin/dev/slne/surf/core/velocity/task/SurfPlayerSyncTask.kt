package dev.slne.surf.core.velocity.task

import com.velocitypowered.api.scheduler.ScheduledTask
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.proxy
import java.util.concurrent.TimeUnit

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
        val onlinePlayers = proxy.allPlayers
            .mapNotNull { surfPlayerService.findPlayerByUuid(it.uniqueId) }

        surfPlayerService.clearPlayers()

        onlinePlayers.forEach { surfPlayerService.cachePlayer(it) }
    }
}
