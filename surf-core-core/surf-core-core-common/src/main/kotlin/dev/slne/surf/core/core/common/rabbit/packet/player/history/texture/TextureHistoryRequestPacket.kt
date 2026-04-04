package dev.slne.surf.core.core.common.rabbit.packet.player.history.texture

import dev.slne.surf.api.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class TextureHistoryRequestPacket(
    val uuid: SerializableUUID
) : RabbitRequestPacket<TextureHistoryResponsePacket>()
