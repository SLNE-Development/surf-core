package dev.slne.surf.core.core.common.rabbit.packet.player.error

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class SaveSurfPlayerErrorRequestPacket(
    val playerUuid: SerializableUUID,
    val occurredOn: String,
    val occurredAt: SerializableOffsetDateTime,
    val staffMessage: String,
    val errorCode: String
) : RabbitRequestPacket<SingleSurfPlayerErrorResponsePacket>()
