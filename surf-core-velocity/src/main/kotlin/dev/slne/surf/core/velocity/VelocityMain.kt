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
import dev.slne.surf.core.core.common.config.SurfServerConfigHolder
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.velocity.listener.SurfPlayerEventListener
import java.nio.file.Path

class VelocityMain @Inject constructor(
    val proxy: ProxyServer,
    val pluginManager: PluginManager,
    val eventManager: EventManager,
    @param:DataDirectory val dataPath: Path,
    val pluginContainer: PluginContainer,
    suspendingPluginContainer: SuspendingPluginContainer
) {
    init {
        suspendingPluginContainer.initialize(this)
        redisLoader.load(dataPath)
        redisApi.subscribeToEvents(SurfPlayerEventListener)
        redisLoader.connect()

        instance = this
        surfServerConfigHolder = SurfServerConfigHolder(dataPath)

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))
    }

    companion object {
        lateinit var instance: VelocityMain
        lateinit var surfServerConfigHolder: SurfServerConfigHolder
    }
}

val proxy get() = VelocityMain.instance.proxy
val plugin get() = VelocityMain.instance
val surfServerConfig get() = VelocityMain.surfServerConfigHolder.config