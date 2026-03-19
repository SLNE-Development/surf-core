package dev.slne.surf.core.core.common.rabbit.packet.player.history.name

import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class NameHistoryResponsePacket(
    val history: NameHistory
) : RabbitResponsePacket()
