package dev.slne.surf.core.minestom.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MinestomCoreConfig(
    val maxPlayers: Int = 100,
)
