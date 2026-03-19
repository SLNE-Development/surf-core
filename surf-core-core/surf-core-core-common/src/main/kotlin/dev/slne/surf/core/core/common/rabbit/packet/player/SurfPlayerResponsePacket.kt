package dev.slne.surf.core.core.common.rabbit.packet.player

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class SurfPlayerResponsePacket(
    val player: SurfPlayer
) : RabbitResponsePacket()
