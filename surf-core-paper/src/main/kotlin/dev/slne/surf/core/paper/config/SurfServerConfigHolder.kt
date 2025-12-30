package dev.slne.surf.core.paper.config

import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

class SurfServerConfigHolder {
    private val configManager: SpongeConfigManager<SurfServerConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            SurfServerConfig::class.java,
            plugin.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(SurfServerConfig::class.java)

        this.reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}