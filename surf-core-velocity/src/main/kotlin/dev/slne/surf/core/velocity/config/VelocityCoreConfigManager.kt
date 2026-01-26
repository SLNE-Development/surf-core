package dev.slne.surf.core.velocity.config

import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

class VelocityCoreConfigManager {
    private val configManager: SpongeConfigManager<VelocityCoreConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            VelocityCoreConfig::class.java,
            plugin.dataPath,
            "velocity-config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            VelocityCoreConfig::class.java
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}