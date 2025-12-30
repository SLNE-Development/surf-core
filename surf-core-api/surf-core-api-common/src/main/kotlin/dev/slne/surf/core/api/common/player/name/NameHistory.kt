package dev.slne.surf.core.api.common.player.name

data class NameHistory(
    val entries: List<NameHistoryEntry>
) {
    val currentName: String? get() = entries.lastOrNull()?.name
}