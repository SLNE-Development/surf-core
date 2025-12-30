package dev.slne.surf.core.paper.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SurfServerConfig(
    val serverName: String = "unknown",
)
