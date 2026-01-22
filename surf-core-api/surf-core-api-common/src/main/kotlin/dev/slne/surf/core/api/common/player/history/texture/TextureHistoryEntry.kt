package dev.slne.surf.core.api.common.player.history.texture

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class TextureHistoryEntry(
    val hash: String,
    val texture: String,
    val signature: String,
    val lastSeen: @Contextual OffsetDateTime
)
