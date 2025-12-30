package dev.slne.surf.core.paper

import dev.slne.surf.core.api.common.event.server.SurfServerStartEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.paper.config.SurfServerConfigHolder
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        redisLoader.load(context.dataDirectory)
        redisLoader.connect()

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
    }
}

val surfServerConfigHolder = SurfServerConfigHolder()
val surfServerConfig get() = surfServerConfigHolder.config