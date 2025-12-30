package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ConnectionListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.launch {
            surfPlayerService.cachePlayer(surfPlayerService.getOrLoadOrCreatePlayerByUuid(event.player.uniqueId))
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        plugin.launch {
            surfPlayerService.findPlayerByUuid(event.player.uniqueId)?.let {
                surfPlayerService.savePlayer(it)
            }

            surfPlayerService.invalidatePlayer(event.player.uniqueId)
        }
    }
}