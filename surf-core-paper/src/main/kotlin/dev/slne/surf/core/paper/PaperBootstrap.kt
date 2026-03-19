package dev.slne.surf.core.paper

import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.config.SurfServerConfigHolder
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.paper.redis.listener.PaperRedisListener
import dev.slne.surf.core.paper.teleport.TeleportRedisListener
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import kotlinx.coroutines.runBlocking

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        runBlocking {
            ClientLoader.onBootstrap()
        }

        CoreInstance.redisApi.subscribeToEvents(TeleportRedisListener)
        ClientLoader.withListener(PaperRedisListener)

        surfServerConfigHolder = SurfServerConfigHolder(context.dataDirectory)

        if (surfServerConfig.serverName.isUnknown() || surfServerConfig.serverDisplayName.isUnknown() || surfServerConfig.serverCategory.isUnknown()) {
            error("Failed to load server config, please check your config file and make sure all fields are filled correctly!")
        }

        val server = SurfServer(
            name = surfServerConfig.serverName,
            displayName = surfServerConfig.serverDisplayName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            maxPlayers = 0
        )

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
        SurfServerService.addServer(server)
    }

    companion object {
        lateinit var surfServerConfigHolder: SurfServerConfigHolder
    }
}

fun String.isUnknown() =
    this.equals("unknown", ignoreCase = true) || this.isBlank() || this.isEmpty()

val surfServerConfig get() = PaperBootstrap.surfServerConfigHolder.config