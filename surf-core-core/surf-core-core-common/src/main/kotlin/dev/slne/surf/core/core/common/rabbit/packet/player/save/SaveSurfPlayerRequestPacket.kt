package dev.slne.surf.core.core.common.rabbit.packet.player.save

import dev.slne.surf.api.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class SaveSurfPlayerRequestPacket(
    val uuid: SerializableUUID,
    val name: String?,
    val firstSeen: SerializableOffsetDateTime?,
    val lastSeen: SerializableOffsetDateTime?,
    val latestServer: String?,
    val latestProxy: String?
) : RabbitRequestPacket<SaveSurfPlayerResponsePacket>()