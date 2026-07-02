package dev.slne.surf.core.core.common.rabbit.packet.player

import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ManyOfflinePlayerNamesResponsePacket(
    val entries: List<OfflinePlayerNameCache.Entry>
) : RabbitResponsePacket()
