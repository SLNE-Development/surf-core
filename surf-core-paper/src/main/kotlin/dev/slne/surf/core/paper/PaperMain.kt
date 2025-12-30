package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.server.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.server.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.ConnectionListener
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        surfEventBus.registerListener(SurfServerEventListener)
    }

    override fun onEnable() {
        ConnectionListener.register()
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
    }

    override fun onDisable() {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))
    }
}