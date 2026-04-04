package dev.slne.surf.core.core.common.config

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SurfServerConfig(
    val serverName: String = "unknown",
    val serverDisplayName: String = "unknown",
    val serverCategory: String = "unknown",
    val serverUuid: SerializableUUID = SerializableUUID.randomUUID()
)
