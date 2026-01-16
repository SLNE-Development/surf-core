package dev.slne.surf.core.api.common.player.history.ip

import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
data class IpAddressHistory(
    val entries: List<IpAddressHistoryEntry>
) {
    fun getLatestIpAddress(): InetAddress? {
        return entries.maxByOrNull { it.lastSeen }?.address
    }
}
