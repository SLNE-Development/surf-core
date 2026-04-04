package dev.slne.surf.core.core.common.rabbit.packet.player.error

import dev.slne.surf.core.api.common.player.error.SurfPlayerError
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class SingleSurfPlayerErrorResponsePacket(
    val error: SurfPlayerError
) : RabbitResponsePacket()
