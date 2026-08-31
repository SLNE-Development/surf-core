package dev.slne.surf.core.velocity.task

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.velocity.player.VelocityPlayerSessionRegistry
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

            val onlineUuids = ObjectOpenHashSet<UUID>(velocityPlayers.size)

            for (velocityPlayer in velocityPlayers) {
                val uuid = velocityPlayer.uniqueId
                onlineUuids += uuid

                val current = VelocityPlayerSessionRegistry[velocityPlayer] ?: continue

                val serverName = velocityPlayer.currentServer.getOrNull()
                    ?.serverInfo
                    ?.name

                val updated = current.copy(
                    currentProxyName = currentProxyName,
                    currentServerName = serverName ?: current.currentServerName,
                )

                if (updated == current) {
                    continue
                }

                if (SurfPlayerService.replacePlayerIfEqualsAndAwait(current, updated)) {
                    VelocityPlayerSessionRegistry.replace(
                        velocityPlayer,
                        current,
                        updated,
                    )
                }
            }

            for (cachedPlayer in currentProxy.getPlayers()) {
                if (cachedPlayer.uuid in onlineUuids) {
                    continue
                }

                if (!SurfPlayerService.invalidatePlayerIfEqualsAndAwait(cachedPlayer)) {
                    continue
                }

                plugin.logger.info(
                    "Found invalid player ${cachedPlayer.uuid} (${cachedPlayer.username}), invalidating cache"
                )
            }
        }
    }
}
