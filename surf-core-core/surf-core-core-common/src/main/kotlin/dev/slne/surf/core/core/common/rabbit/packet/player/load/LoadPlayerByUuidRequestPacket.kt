package dev.slne.surf.core.core.common.rabbit.packet.player.load

import dev.slne.surf.core.core.common.rabbit.packet.player.OptionalSurfPlayerResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class LoadPlayerByUuidRequestPacket(
    val uuid: SerializableUUID
) : RabbitRequestPacket<OptionalSurfPlayerResponsePacket>()