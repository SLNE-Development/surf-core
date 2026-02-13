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
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.core.common.config.SurfServerConfigHolder
import dev.slne.surf.core.core.common.database.databaseLoader
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.velocity.auth.AuthenticationListener
import dev.slne.surf.core.velocity.auth.authentificationService
import dev.slne.surf.core.velocity.config.VelocityCoreConfigManager
import dev.slne.surf.core.velocity.listener.ConnectionListener
import dev.slne.surf.core.velocity.listener.VelocityServerListener
import dev.slne.surf.core.velocity.redis.handler.VelocityRedisResponseHandler
import dev.slne.surf.core.velocity.redis.listener.VelocityRedisListener
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.coroutines.runBlocking
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

        instance = this
        surfServerConfigHolder = SurfServerConfigHolder(dataPath)
        redisLoader.load()
        surfPlayerService.init()
        surfServerService.init()
        authentificationService.init()
        redisLoader.withListener(VelocityRedisListener)
        redisLoader.withRequestResponseHandler(VelocityRedisResponseHandler)
        redisLoader.connect()

        val server = SurfServer(
            name = surfServerConfig.serverName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            type = SurfServerType.PROXY,
            maxPlayers = plugin.proxy.configuration.showMaxPlayers,
            connectionAddress = InetSocketAddress(
                surfServerConfig.connectionAddress.host,
                surfServerConfig.connectionAddress.port
            )
        )

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
        surfServerService.addServer(server)
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        runBlocking {
            databaseLoader.connect(dataPath)
        }

        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
        eventManager.register(this, ConnectionListener)
        eventManager.register(this, AuthenticationListener)
        eventManager.register(this, VelocityServerListener)

        surfServerService.changeState(SurfServer.current(), SurfServerState.RUNNING)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))

        surfServerService.changeState(SurfServer.current(), SurfServerState.STOPPING)
        surfServerService.removeServer(SurfServer.current())

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

        redisLoader.disconnect()
        databaseLoader.disconnect()
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