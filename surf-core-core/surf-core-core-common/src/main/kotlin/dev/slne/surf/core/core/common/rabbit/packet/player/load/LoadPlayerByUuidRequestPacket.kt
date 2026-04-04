package dev.slne.surf.core.core.common.rabbit.packet.player.load

import dev.slne.surf.api.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.core.common.rabbit.packet.player.OptionalSurfPlayerResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class LoadPlayerByUuidRequestPacket(
    val uuid: SerializableUUID
) : RabbitRequestPacket<OptionalSurfPlayerResponsePacket>()