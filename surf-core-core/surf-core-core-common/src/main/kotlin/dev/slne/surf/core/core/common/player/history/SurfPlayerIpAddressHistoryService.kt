package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.core.api.common.player.history.IpAddressHistory
import dev.slne.surf.core.api.common.player.history.entry.IpAddressHistoryEntry
import dev.slne.surf.surfapi.core.api.util.requiredService
import java.util.*

val surfPlayerIpAddressHistoryService = requiredService<SurfPlayerIpAddressHistoryService>()

interface SurfPlayerIpAddressHistoryService {
    suspend fun addIpAddressToHistory(uuid: UUID, historyEntry: IpAddressHistoryEntry)
    suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory
}