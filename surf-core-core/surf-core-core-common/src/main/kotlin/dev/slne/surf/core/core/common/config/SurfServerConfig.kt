package dev.slne.surf.core.core.common.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SurfServerConfig(
    val serverName: String = "unknown",
    val serverCategory: String = "unknown"
)
