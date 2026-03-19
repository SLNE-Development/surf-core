package dev.slne.surf.core.core.common.rabbit.packet.player.history.ip

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
class SaveIpAddressHistoryResponsePacket : RabbitResponsePacket()