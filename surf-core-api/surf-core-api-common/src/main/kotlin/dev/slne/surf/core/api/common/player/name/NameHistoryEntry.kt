package dev.slne.surf.core.api.common.player.name

import kotlinx.serialization.Serializable

@Serializable
data class NameHistoryEntry(
    val name: String,
    val changedAt: Long?
)
