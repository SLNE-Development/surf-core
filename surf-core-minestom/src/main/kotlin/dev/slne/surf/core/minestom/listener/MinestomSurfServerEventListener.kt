package dev.slne.surf.core.minestom.listener

import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.core.api.common.event.SurfEventHandler
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.event.SurfServerNotificationService
import dev.slne.surf.core.core.common.permission.CorePermissions

object MinestomSurfServerEventListener {
    @SurfEventHandler
    fun onServerStart(event: SurfServerStartEvent) {
        SurfServerNotificationService.starting(notifyPlayers(), event.serverName)
    }

    @SurfEventHandler
    fun onServerOnline(event: SurfServerOnlineEvent) {
        SurfServerNotificationService.online(notifyPlayers(), event.serverName)
    }

    @SurfEventHandler
    fun onServerStop(event: SurfServerStoppingEvent) {
        SurfServerNotificationService.stopping(notifyPlayers(), event.serverName)
    }

    private fun notifyPlayers() = ConnectionManager.onlinePlayers
        .filter { it.hasPermission(CorePermissions.SERVER_NOTIFY) }
}
