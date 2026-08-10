package dev.slne.surf.core.paper.event

import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.api.paper.util.getOrCreatePermission
import dev.slne.surf.core.core.common.event.SurfServerNotificationService
import org.bukkit.Bukkit

object SurfServerEventListener {
    @SurfEventHandler
    fun onServerStart(event: SurfServerStartEvent) {
        SurfServerNotificationService.starting(
            Bukkit.getOnlinePlayers().filter { it.hasPermission(CorePermissions.SERVER_NOTIFY) },
            event.serverName,
        )

        pluginManager.getOrCreatePermission("surf.core.server.${event.serverName}")
    }

    @SurfEventHandler
    fun onServerOnline(event: SurfServerOnlineEvent) {
        SurfServerNotificationService.online(
            Bukkit.getOnlinePlayers().filter { it.hasPermission(CorePermissions.SERVER_NOTIFY) },
            event.serverName,
        )
    }

    @SurfEventHandler
    fun onServerStop(event: SurfServerStoppingEvent) {
        SurfServerNotificationService.stopping(
            Bukkit.getOnlinePlayers().filter { it.hasPermission(CorePermissions.SERVER_NOTIFY) },
            event.serverName,
        )
    }
}
