package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.core.paper.surfServerConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ConnectionListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.launch {
            val player = surfPlayerService.getOrLoadOrCreatePlayerByUuid(
                event.player.uniqueId
            ).apply {
                if (firstSeen == null) {
                    firstSeen = System.currentTimeMillis()
                }
                lastSeen = System.currentTimeMillis()
                lastKnownName = event.player.name
                currentServer = surfServerConfig.serverName
            }


            surfEventBus.fire(
                SurfPlayerConnectEvent(
                    player
                )
            )

            surfPlayerService.savePlayer(player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = surfPlayerService.findPlayerByUuid(event.player.uniqueId) ?: return

        surfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        plugin.launch {
            surfPlayerService.savePlayer(player.apply {
                lastSeen = System.currentTimeMillis()
            })
        }
    }
}