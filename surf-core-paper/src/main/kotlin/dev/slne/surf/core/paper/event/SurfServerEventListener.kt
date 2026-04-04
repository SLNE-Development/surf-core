package dev.slne.surf.core.paper.event

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.paper.util.getOrCreatePermission
import org.bukkit.Bukkit

object SurfServerEventListener {
    @SurfEventHandler
    fun onServerStart(event: SurfServerStartEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendStartingPrefix()
                info("Der Server ")
                variableValue(event.serverName)
                info(" startet nun...")
            }
        }

        pluginManager.getOrCreatePermission("surf.core.server.${event.serverName}")
    }

    @SurfEventHandler
    fun onServerOnline(event: SurfServerOnlineEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendOnlinePrefix()
                info("Der Server ")
                variableValue(event.serverName)
                info(" ist nun online.")
            }
        }
    }

    @SurfEventHandler
    fun onServerStop(event: SurfServerStoppingEvent) {
        Bukkit.getOnlinePlayers().filter { it.hasPermission("surf.core.servernotify") }.forEach {
            it.sendText {
                appendStoppingPrefix()
                info("Der Server ")
                variableValue(event.serverName)
                info(" stoppt nun...")
            }
        }
    }

    private fun SurfComponentBuilder.appendStartingPrefix() = append {
        text("»", Colors.YELLOW)
        darkSpacer(" |")
        appendSpace()
    }

    private fun SurfComponentBuilder.appendOnlinePrefix() = append {
        success("»")
        darkSpacer(" |")
        appendSpace()
    }

    private fun SurfComponentBuilder.appendStoppingPrefix() = append {
        error("»")
        darkSpacer(" |")
        appendSpace()
    }
}