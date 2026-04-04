package dev.slne.surf.core.core.common.rabbit.packet.player.history.name

import dev.slne.surf.api.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class NameHistoryRequestPacket(
    val uuid: SerializableUUID
) : RabbitRequestPacket<NameHistoryResponsePacket>()
