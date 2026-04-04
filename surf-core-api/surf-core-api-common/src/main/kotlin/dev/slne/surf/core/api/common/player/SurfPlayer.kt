package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.serializer.SurfPlayerSerializer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

@Serializable(with = SurfPlayerSerializer::class)
data class SurfPlayer(
    val uuid: @Contextual UUID,
    var lastKnownName: String?,
    var firstSeen: @Contextual OffsetDateTime?,
    var lastSeen: @Contextual OffsetDateTime?,
    var currentServer: SurfServer? = null,
    var currentProxy: SurfProxyServer? = null,
    var lastKnownIpAddress: @Contextual InetAddress? = null,
    var transferred: Boolean
) {
    val username get() = lastKnownName ?: "#Unbekannt"
    fun isOnline() = SurfCoreApi.getOnlinePlayers().any { it.uuid == uuid }
    suspend fun sendAwaiting(server: SurfServer) = SurfCoreApi.sendPlayerAwaiting(this, server)
    suspend fun sendAwaiting(proxy: SurfProxyServer) = SurfCoreApi.sendPlayerAwaiting(this, proxy)

    override fun toString(): String {
        return "SurfPlayer(uuid=$uuid, lastKnownName=$lastKnownName, firstSeen=$firstSeen, lastSeen=$lastSeen, currentServer=${currentServer?.name}, currentProxy=${currentProxy?.name}, lastKnownIpAddress=$lastKnownIpAddress)"
    }

}