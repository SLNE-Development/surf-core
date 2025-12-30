package dev.slne.surf.core.paper

import dev.slne.surf.core.api.common.event.SurfServerStartEvent
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

        surfServerConfigHolder = SurfServerConfigHolder(context.dataDirectory)

        surfEventBus.fire(SurfServerStartEvent(surfServerConfig.serverName))
    }

    companion object {
        lateinit var surfServerConfigHolder: SurfServerConfigHolder
    }
}

val surfServerConfig get() = PaperBootstrap.surfServerConfigHolder.config