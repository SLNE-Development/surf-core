package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.paper.command.*
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.PlayerConnectListener
import dev.slne.surf.core.paper.task.surfServerInformationSyncTask
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        ClientLoader.onLoad()

        surfEventBus.registerListener(SurfServerEventListener)
    }

    override suspend fun onEnableAsync() {
        ClientLoader.onEnable()

        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
        SurfServerService.changeState(SurfServer.current(), SurfServerState.RUNNING)

        lastSeenCommand()
        networkListCommand()
        networkTeleportCommand()
        surfCoreCommand()
        whereAmICommand()
        networkInformationCommand()
        networkServerCommand()
        networkBroadcastCommand()
        networkSendCommand()
        networkServerMaxPlayersCommand()
        hubCommand()

        PlayerConnectListener.register()

        SurfServerService.addServer(
            SurfServer.current().copy(maxPlayers = Bukkit.getMaxPlayers())
        )

        surfServerInformationSyncTask.start()
    }

    override suspend fun onDisableAsync() {
        surfServerInformationSyncTask.stop()
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))
        SurfServerService.changeState(SurfServer.current(), SurfServerState.STOPPING)
        SurfServerService.removeServer(SurfServer.current())

        ClientLoader.onDisable()
    }
}