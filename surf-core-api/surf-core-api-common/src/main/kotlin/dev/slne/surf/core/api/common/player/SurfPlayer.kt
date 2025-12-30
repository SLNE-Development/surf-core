package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.player.name.NameHistory
import dev.slne.surf.core.api.common.surfCoreApi
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SurfPlayer(
    val uuid: @Contextual UUID,
    val firstSeen: Long?,
    val lastSeen: Long?,
    val nameHistory: NameHistory
) {
    var currentServer: String? = null
    val currentName get() = nameHistory.currentName

    fun isOnline() = surfCoreApi.getOnlinePlayers().any { it.uuid == uuid }
}