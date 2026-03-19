package dev.slne.surf.core.core.common.rabbit.packet.player.save

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class SaveSurfPlayerRequestPacket(
    val player: SurfPlayer
) : RabbitRequestPacket<SaveSurfPlayerResponsePacket>()