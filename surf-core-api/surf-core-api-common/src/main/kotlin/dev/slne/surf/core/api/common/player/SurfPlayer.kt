package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.player.serializer.SurfPlayerSerializer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.surfCoreApi
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
    var currentServer: CommonSurfServer? = null,
    var currentProxy: CommonSurfServer? = null,
    var lastKnownIpAddress: @Contextual InetAddress? = null
) {
    fun isOnline() = surfCoreApi.getOnlinePlayers().any { it.uuid == uuid }
    fun send(server: CommonSurfServer) = surfCoreApi.sendPlayer(this, server)

    override fun toString(): String {
        return "SurfPlayer(uuid=$uuid, lastKnownName=$lastKnownName, firstSeen=$firstSeen, lastSeen=$lastSeen, currentServer=${currentServer?.name}, currentProxy=${currentProxy?.name}, lastKnownIpAddress=$lastKnownIpAddress)"
    }

}