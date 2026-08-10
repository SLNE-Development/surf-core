package dev.slne.surf.core.minestom.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import java.nio.file.Path

class MinestomCoreConfigManager(dataPath: Path) {
    private val configManager: SpongeConfigManager<MinestomCoreConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            MinestomCoreConfig::class.java,
            dataPath,
            "minestom-config.yml",
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            MinestomCoreConfig::class.java,
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}
