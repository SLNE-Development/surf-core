package dev.slne.surf.core.core.common.config

import dev.slne.surf.surfapi.core.api.serializer.java.ip.inetsocket.SerializableInetSocketAddress
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.net.InetSocketAddress

@ConfigSerializable
data class SurfServerConfig(
    val serverName: String = "unknown",
    val serverCategory: String = "unknown",
    val connectionAddress: SerializableInetSocketAddress = InetSocketAddress("localhost", 25565)
)
