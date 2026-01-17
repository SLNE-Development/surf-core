package dev.slne.surf.core.api.common.player.history.ip

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.time.OffsetDateTime

@Serializable
data class IpAddressHistoryEntry(
    val address: @Contextual InetAddress,
    val lastSeen: @Contextual OffsetDateTime
)
