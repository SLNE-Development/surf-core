package dev.slne.surf.core.api.common.player.history.entry

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
data class IpAddressHistoryEntry(
    val address: @Contextual InetAddress,
    val lastSeen: Long
)
