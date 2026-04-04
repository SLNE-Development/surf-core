package dev.slne.surf.core.core.common.config

import dev.slne.surf.api.core.config.SurfConfigApi
import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import java.nio.file.Path

class SurfServerConfiguration(
    dataPath: Path
) {
    private val configManager: SpongeConfigManager<SurfServerConfig>

    init {
        SurfConfigApi.createSpongeYmlConfig(
            SurfServerConfig::class.java,
            dataPath,
            "config.yml"
        )

        configManager = SurfConfigApi.getSpongeConfigManagerForConfig(SurfServerConfig::class.java)

        this.reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}