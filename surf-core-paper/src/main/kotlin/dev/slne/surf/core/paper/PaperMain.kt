package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.database.databaseLoader
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.paper.command.lastSeenCommand
import dev.slne.surf.core.paper.command.networkListCommand
import dev.slne.surf.core.paper.command.networkTeleportCommand
import dev.slne.surf.core.paper.event.SurfPlayerEventListener
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.ConnectionListener
import dev.slne.surf.surfapi.bukkit.api.event.register
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        surfEventBus.registerListener(SurfServerEventListener)
        surfEventBus.registerListener(SurfPlayerEventListener)

        runBlocking {
            databaseLoader.connect(dataPath)
        }
    }

    override fun onEnable() {
        ConnectionListener.register()
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))

        lastSeenCommand()
        networkListCommand()
        networkTeleportCommand()
    }

    override fun onDisable() {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))

        databaseLoader.disconnect()
        redisApi.disconnect()
    }
}