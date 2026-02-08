package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.core.common.database.databaseLoader
import dev.slne.surf.core.core.common.error.GlobalErrorHandler
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.paper.command.*
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.PlayerConnectListener
import dev.slne.surf.surfapi.bukkit.api.event.register
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        // Install global error handler early
        GlobalErrorHandler.install()
        
        surfEventBus.registerListener(SurfServerEventListener)
    }

    override fun onEnable() {
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
        surfServerService.changeState(SurfServer.current(), SurfServerState.RUNNING)

        lastSeenCommand()
        networkListCommand()
        networkTeleportCommand()
        surfCoreCommand()
        whereAmICommand()
        networkInformationCommand()
        networkServerCommand()
        networkBroadcastCommand()
        networkSendCommand()
        coreErrorCommand()
        systemErrorCommand()

        PlayerConnectListener.register()

        surfServerService.addServer(SurfServer.current().copy(maxPlayers = Bukkit.getMaxPlayers()))

        runBlocking {
            databaseLoader.connect(plugin.dataPath)
        }
    }

    override fun onDisable() {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))
        surfServerService.changeState(SurfServer.current(), SurfServerState.STOPPING)
        surfServerService.removeServer(SurfServer.current())

        redisLoader.disconnect()
        databaseLoader.disconnect()
    }
}