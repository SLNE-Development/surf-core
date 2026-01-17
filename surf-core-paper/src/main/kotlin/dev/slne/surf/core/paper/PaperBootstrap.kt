package dev.slne.surf.core.paper

import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.core.common.config.SurfServerConfigHolder
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.paper.teleport.TeleportRedisListener
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        redisLoader.load()
        redisApi.subscribeToEvents(TeleportRedisListener)
        surfPlayerService.init()
        surfServerService.init()
        redisLoader.connect()

        surfServerConfigHolder = SurfServerConfigHolder(context.dataDirectory)

        val server = SurfServer(
            name = surfServerConfig.serverName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            type = SurfServerType.SERVER
        )

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
        surfServerService.addServer(server)
    }

    companion object {
        lateinit var surfServerConfigHolder: SurfServerConfigHolder
    }
}

val surfServerConfig get() = PaperBootstrap.surfServerConfigHolder.config