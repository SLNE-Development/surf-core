package dev.slne.surf.core.api.common.player.history

import dev.slne.surf.core.api.common.player.history.entry.NameHistoryEntry
import kotlinx.serialization.Serializable

@Serializable
data class NameHistory(
    val entries: List<NameHistoryEntry>
) {
    fun getCurrentName(): String? {
        return entries.maxByOrNull { it.lastSeen }?.name
    }
}
