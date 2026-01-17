package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.util.*

@Serializable
data class SurfPlayer(
    val uuid: @Contextual UUID,
    var lastKnownName: String?,
    var firstSeen: Long?,
    var lastSeen: Long?,
    var currentServer: SurfServer? = null,
    var currentProxy: SurfServer? = null,
    var lastKnownIpAddress: @Contextual InetAddress? = null
) {
    fun isOnline() = surfCoreApi.getOnlinePlayers().any { it.uuid == uuid }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurfPlayer) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}