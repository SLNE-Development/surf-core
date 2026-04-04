package dev.slne.surf.core.core.common.config

import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import java.nio.file.Path

class SurfServerConfiguration(
    dataPath: Path
) {
    private val configManager: SpongeConfigManager<SurfServerConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            SurfServerConfig::class.java,
            dataPath,
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