package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import dev.slne.surf.surfapi.core.api.util.requiredService
import java.util.*

val surfPlayerIpAddressHistoryService = requiredService<SurfPlayerIpAddressHistoryService>()

interface SurfPlayerIpAddressHistoryService {
    suspend fun handleNewIpAddress(surfPlayer: SurfPlayer)
    suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory
}