package dev.slne.surf.core.core.common.rabbit.packet.player.history.texture

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class SaveTextureHistoryRequestPacket(
    val uuid: SerializableUUID,
    val texture: String,
    val signature: String,
    val skinHash: String
) : RabbitRequestPacket<SaveTextureHistoryResponsePacket>()
