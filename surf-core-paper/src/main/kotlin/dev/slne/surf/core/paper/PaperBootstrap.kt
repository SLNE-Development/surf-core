package dev.slne.surf.core.paper

import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.paper.CorePlayerInfoProvider
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.config.SurfServerConfiguration
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.paper.api.DefaultCorePlayerInfoProvider
import dev.slne.surf.core.paper.redis.listener.PaperRedisListener
import dev.slne.surf.core.paper.teleport.TeleportRedisListener
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        PaperBootstrap.context = context
        runBlocking {
            ClientCoreInstance.clientLoader.onBootstrap()
            ClientCoreInstance.clientLoader.onLoad()
        }

        CoreInstance.redisApi.subscribeToEvents(TeleportRedisListener)
        ClientCoreInstance.clientLoader.withListener(PaperRedisListener)
        ClientCoreInstance.clientLoader.connectRedis()

        surfServerConfiguration = SurfServerConfiguration(context.dataDirectory)

        if (surfServerConfig.serverName.isUnknown() || surfServerConfig.serverDisplayName.isUnknown() || surfServerConfig.serverCategory.isUnknown()) {
            error("Failed to load server config, please check your config file and make sure all fields are filled correctly!")
        }

        val server = SurfServer(
            name = surfServerConfig.serverName,
            displayName = surfServerConfig.serverDisplayName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            maxPlayers = 0,
            uuid = surfServerConfig.serverUuid,
            startedAt = OffsetDateTime.now()
        )

        SurfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
        SurfServerService.addServer(server)

        CorePlayerInfoProvider.setInstance(DefaultCorePlayerInfoProvider)
    }

    companion object {
        lateinit var context: BootstrapContext
        lateinit var surfServerConfiguration: SurfServerConfiguration
    }
}

fun String.isUnknown() =
    this.equals("unknown", ignoreCase = true) || this.isBlank() || this.isEmpty()

val surfServerConfig get() = PaperBootstrap.surfServerConfiguration.config