package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.database.databaseLoader
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.paper.command.*
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.PlayerClientLoadedListener
import dev.slne.surf.surfapi.bukkit.api.event.register
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        surfEventBus.registerListener(SurfServerEventListener)
    }

    override fun onEnable() {
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))

        lastSeenCommand()
        networkListCommand()
        networkTeleportCommand()
        surfCoreCommand()
        whereAmICommand()
        networkInformationCommand()

        PlayerClientLoadedListener.register()

        runBlocking {
            databaseLoader.connect(plugin.dataPath)
        }
    }

    override fun onDisable() {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))

        redisLoader.disconnect()
        databaseLoader.disconnect()
    }
}