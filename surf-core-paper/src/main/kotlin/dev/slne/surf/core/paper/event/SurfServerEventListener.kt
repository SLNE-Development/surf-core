package dev.slne.surf.core.paper.event

import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.server.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.server.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.server.SurfServerStoppingEvent
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Bukkit

object SurfServerEventListener {
    @SurfEventHandler
    fun onServerStart(event: SurfServerStartEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendPrefix()
                info("Der Server ")
                variableValue(event.serverName)
                success(" startet nun...")
            }
        }
    }

    @SurfEventHandler
    fun onServerOnline(event: SurfServerOnlineEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendPrefix()
                info("Der Server ")
                variableValue(event.serverName)
                success(" ist nun online")
            }
        }
    }

    @SurfEventHandler
    fun onServerStop(event: SurfServerStoppingEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendPrefix()
                info("Der Server ")
                variableValue(event.serverName)
                error(" stoppt nun...")
            }
        }
    }
}