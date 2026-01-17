package dev.slne.surf.core.api.common.player.history.name

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class NameHistoryEntry(
    val name: String,
    val lastSeen: @Contextual OffsetDateTime
)
