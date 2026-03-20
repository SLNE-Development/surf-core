package dev.slne.surf.core.core.common.rabbit.packet.player.history.texture

import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class TextureHistoryResponsePacket(
    val history: TextureHistory
) : RabbitResponsePacket()