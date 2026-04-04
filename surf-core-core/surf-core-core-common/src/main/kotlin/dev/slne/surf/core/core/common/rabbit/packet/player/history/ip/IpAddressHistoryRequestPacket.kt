package dev.slne.surf.core.core.common.rabbit.packet.player.history.ip

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class IpAddressHistoryRequestPacket(
    val uuid: SerializableUUID
) : RabbitRequestPacket<IpAddressHistoryResponsePacket>()