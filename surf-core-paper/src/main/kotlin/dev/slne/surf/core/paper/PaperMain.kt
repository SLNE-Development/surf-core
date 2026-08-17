package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.paper.command.*
import dev.slne.surf.core.paper.event.SurfServerEventListener
import dev.slne.surf.core.paper.listener.PlayerConnectListener
import dev.slne.surf.core.paper.task.surfServerInformationSyncTask
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        SurfEventBus.registerListener(SurfServerEventListener)
    }

    override suspend fun onEnableAsync() {
        ClientCoreInstance.clientLoader.onEnable()

        SurfServerService.changeState(SurfServer.current(), SurfServerState.RUNNING)

        if (System.getProperty(LauncherConstants.PROPERTY_LAUNCHED_BY_CORE) == null) {
            SurfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
        }

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


        luckPerms()

        surfServerInformationSyncTask.start()
        OfflinePlayerNameCache.startPulling(plugin.scope)
    }

    override suspend fun onDisableAsync() {
        surfServerInformationSyncTask.stop()
        SurfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))
        SurfServerService.changeState(SurfServer.current(), SurfServerState.STOPPING)
        SurfServerService.removeServer(SurfServer.current())

        ClientCoreInstance.clientLoader.onDisable()
    }


    private fun luckPerms() {
        LuckPermsAccess.luckperms.eventBus.subscribe(
            plugin,
            UserDataRecalculateEvent::class.java
        ) { event ->
            Bukkit.getPlayer(event.user.uniqueId)?.let {
                it.displayName(it.getPrefixedName())
            }
        }
    }
}