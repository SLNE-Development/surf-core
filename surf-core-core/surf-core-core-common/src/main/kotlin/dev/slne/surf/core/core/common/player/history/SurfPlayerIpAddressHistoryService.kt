package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import java.util.*

private val service = requiredService<SurfPlayerIpAddressHistoryService>()

interface SurfPlayerIpAddressHistoryService {
    suspend fun handleNewIpAddress(surfPlayer: SurfPlayer)
    suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory

    companion object : SurfPlayerIpAddressHistoryService by service {
        val INSTANCE get() = service
    }
}