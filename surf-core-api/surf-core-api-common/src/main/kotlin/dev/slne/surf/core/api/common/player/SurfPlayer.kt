package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.surfCoreApi
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SurfPlayer(
    val uuid: @Contextual UUID,
    var lastKnownName: String?,
    var firstSeen: Long?,
    var lastSeen: Long?
) {
    var currentServer: String? = null
    fun isOnline() = surfCoreApi.getOnlinePlayers().any { it.uuid == uuid }
}