package dev.slne.surf.core.core.common.rabbit.packet.player.error

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class SaveSurfPlayerErrorRequestPacket(
    val playerUuid: SerializableUUID,
    val occurredOn: String,
    val occurredAt: SerializableOffsetDateTime,
    val staffMessage: String,
    val errorCode: String
) : RabbitRequestPacket<SingleSurfPlayerErrorResponsePacket>()
