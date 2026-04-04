package dev.slne.surf.core.api.common.player.error

import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class SurfPlayerError(
    val playerUuid: SerializableUUID,
    val occurredOn: String,
    val occurredAt: SerializableOffsetDateTime,
    val staffMessage: String,
    val errorCode: String,
)
