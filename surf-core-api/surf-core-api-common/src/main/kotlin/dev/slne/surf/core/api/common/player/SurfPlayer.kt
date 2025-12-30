package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.player.name.NameHistory
import dev.slne.surf.core.api.common.surfCoreApi
import java.util.*

data class SurfPlayer(
    val uuid: UUID,
    val firstSeen: Long?,
    val lastSeen: Long?,
    val nameHistory: NameHistory
) {
    var currentServer: String? = null
    val currentName get() = nameHistory.currentName

    fun isOnline() = surfCoreApi.getOnlinePlayers().any { it.uuid == uuid }
}