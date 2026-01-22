package dev.slne.surf.core.api.common.player.history.texture

import kotlinx.serialization.Serializable

@Serializable
data class TextureHistory(
    val entries: List<TextureHistoryEntry>
) {
    fun getCurrentTexture(): TextureHistoryEntry? = entries.maxByOrNull { it.lastSeen }
}
