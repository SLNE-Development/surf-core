package dev.slne.surf.core.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.common.config.SurfServerConfigHolder
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.velocity.auth.AuthenticationListener
import dev.slne.surf.core.velocity.auth.authenticationService
import dev.slne.surf.core.velocity.config.VelocityCoreConfigManager
import dev.slne.surf.core.velocity.listener.ConnectionListener
import dev.slne.surf.core.velocity.listener.VelocityServerListener
import dev.slne.surf.core.velocity.redis.handler.SendPlayerToProxyHandler
import dev.slne.surf.core.velocity.redis.handler.SendPlayerToServerHandler
import dev.slne.surf.core.velocity.redis.listener.VelocityRedisListener
import dev.slne.surf.core.velocity.task.surfPlayerSyncTask
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.format.TextDecoration
import org.slf4j.Logger
import java.net.InetSocketAddress
import java.nio.file.Path

class VelocityMain @Inject constructor(
    val proxy: ProxyServer,
    val pluginManager: PluginManager,
    val eventManager: EventManager,
    @param:DataDirectory val dataPath: Path,
    val pluginContainer: PluginContainer,
    val logger: Logger,
    suspendingPluginContainer: SuspendingPluginContainer
) {
    init {
        suspendingPluginContainer.initialize(this)

        runBlocking {
            ClientLoader.onBootstrap()
            ClientLoader.onLoad()
        }

        instance = this
        surfServerConfigHolder = SurfServerConfigHolder(dataPath)

        authenticationService.init()

        ClientLoader.withListener(VelocityRedisListener)
        ClientLoader.withRequestResponseHandler(SendPlayerToProxyHandler)
        ClientLoader.withRequestResponseHandler(SendPlayerToServerHandler)
        ClientLoader.connectRedis()

        val server = SurfProxyServer(
            name = surfServerConfig.serverName,
            displayName = surfServerConfig.serverDisplayName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            maxPlayers = plugin.proxy.configuration.showMaxPlayers,
            address = InetSocketAddress(
                velocityCoreConfigManager.config.connectionAddress.host,
                velocityCoreConfigManager.config.connectionAddress.port
            )
        )

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
        SurfServerService.addServer(server)
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        runBlocking {
            ClientLoader.onEnable()
        }

        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
        eventManager.register(this, ConnectionListener)
        eventManager.register(this, AuthenticationListener)
        eventManager.register(this, VelocityServerListener)

        SurfServerService.changeState(SurfProxyServer.current(), SurfServerState.RUNNING)

        surfPlayerSyncTask.start()
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        surfPlayerSyncTask.stop()

        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))

        SurfCoreApi.getOnlinePlayers().forEach {
            it.sendText {
                appendInfoPrefix()
                error("SYSTEM-NEUSTART", TextDecoration.BOLD)
                spacer(": ")
                spacer("Derzeit werden Hintergrundsysteme neugestartet. Bitte habt Verständnis, sollten in diesem Zeitraum Probleme auftreten!")
            }
        }

        SurfServerService.changeState(SurfProxyServer.current(), SurfServerState.STOPPING)
        SurfServerService.removeServer(SurfProxyServer.current())

        proxy.allPlayers.forEach {
            it.disconnect(buildText {
                appendKickDisconnectMessage(
                    {
                        variableValue("Der Proxy wird heruntergefahren...")
                    }, {
                        spacer("Bitte verbinde dich später erneut.")
                    }
                )
            })
        }

        runBlocking {
            ClientLoader.onDisable()
        }
    }

    companion object {
        lateinit var instance: VelocityMain
        lateinit var surfServerConfigHolder: SurfServerConfigHolder
    }
}

val velocityCoreConfigManager = VelocityCoreConfigManager()

val proxy get() = VelocityMain.instance.proxy
val plugin get() = VelocityMain.instance
val surfServerConfig get() = VelocityMain.surfServerConfigHolder.config