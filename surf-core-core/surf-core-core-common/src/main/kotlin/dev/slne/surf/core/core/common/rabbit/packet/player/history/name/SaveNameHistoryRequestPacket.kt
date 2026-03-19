package dev.slne.surf.core.core.common.rabbit.packet.player.history.name

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class SaveNameHistoryRequestPacket(
    val uuid: SerializableUUID,
    val name: String
) : RabbitRequestPacket<SaveNameHistoryResponsePacket>()
