package dev.slne.surf.core.velocity.config

import dev.slne.surf.api.core.config.SurfConfigApi
import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.core.velocity.plugin

class VelocityCoreConfigManager {
    private val configManager: SpongeConfigManager<VelocityCoreConfig>

    init {
        SurfConfigApi.createSpongeYmlConfig(
            VelocityCoreConfig::class.java,
            plugin.dataPath,
            "velocity-config.yml"
        )
        configManager = SurfConfigApi.getSpongeConfigManagerForConfig(
            VelocityCoreConfig::class.java
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}