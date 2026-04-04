package dev.slne.surf.core.paper.task

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.paper.plugin
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

val surfServerInformationSyncTask = SurfServerInformationSyncTask()

class SurfServerInformationSyncTask {
    lateinit var task: ScheduledTask
    private var latestMaxPlayers: Int = Bukkit.getMaxPlayers()

    fun start() {
        task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            val currentMaxPlayers = Bukkit.getMaxPlayers()

            if (currentMaxPlayers != latestMaxPlayers) {
                latestMaxPlayers = currentMaxPlayers

                SurfServerService.addServer(
                    SurfServer.current().copy(
                        maxPlayers = currentMaxPlayers
                    )
                )
            }
        }, 0L, 1L, TimeUnit.SECONDS)
    }

    fun stop() {
        if (::task.isInitialized && !task.isCancelled) {
            task.cancel()
        }
    }
}