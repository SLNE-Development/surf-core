package dev.slne.surf.core.core.common.rabbit.packet.player.history.ip

import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class IpAddressHistoryResponsePacket(
    val history: IpAddressHistory
) : RabbitResponsePacket()
