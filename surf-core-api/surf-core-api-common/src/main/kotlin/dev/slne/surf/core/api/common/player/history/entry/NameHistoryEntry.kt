package dev.slne.surf.core.api.common.player.history.entry

import kotlinx.serialization.Serializable

@Serializable
data class NameHistoryEntry(
    val name: String,
    val lastSeen: Long
)
