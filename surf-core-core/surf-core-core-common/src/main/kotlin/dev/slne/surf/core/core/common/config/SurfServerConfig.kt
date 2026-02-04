package dev.slne.surf.core.core.common.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SurfServerConfig(
    val serverName: String = "unknown",
    val serverCategory: String = "unknown",
    val connectionAddress: SurfServerConnectionAddressConfig = SurfServerConnectionAddressConfig(
        "localhost",
        25565
    )
)

@ConfigSerializable
data class SurfServerConnectionAddressConfig(
    val host: String = "localhost",
    val port: Int = 25565
)
