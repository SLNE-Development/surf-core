package dev.slne.surf.core.velocity.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VelocityCoreConfig(
    val teamDomain: String = "team.castcrafter.de",
    val blockedDomains: List<String> = listOf(
        "proxy01.castcrafter.de",
        "proxy02.castcrafter.de",
        "proxy03.castcrafter.de",
        "proxy04.castcrafter.de",
        "proxy05.castcrafter.de",
        "proxy06.castcrafter.de",
        "proxy07.castcrafter.de",
        "proxy08.castcrafter.de",
        "proxy09.castcrafter.de",
        "proxy10.castcrafter.de",
        "dev01.castcrafter.de",
        "dev02.castcrafter.de",
        "dev03.castcrafter.de",
        "dev04.castcrafter.de",
        "dev05.castcrafter.de"
    ),
    val connectionAddress: SurfServerConnectionAddressConfig = SurfServerConnectionAddressConfig(
        "localhost",
        25565
    )
) {
    @ConfigSerializable
    data class SurfServerConnectionAddressConfig(
        val host: String = "localhost",
        val port: Int = 25565
    )
}
