package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistoryEntry
import dev.slne.surf.core.core.common.player.history.SurfPlayerIpAddressHistoryService
import dev.slne.surf.core.fallback.repository.surfPlayerIpAddressHistoryRepository
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*

@AutoService(SurfPlayerIpAddressHistoryService::class)
class SurfPlayerIpAddressHistoryServiceImpl : SurfPlayerIpAddressHistoryService, Services.Fallback {
    override suspend fun handleNewIpAddress(surfPlayer: SurfPlayer) {
        val currentIp = surfPlayer.lastKnownIpAddress ?: return
        val latestLogged = getIpAddressHistory(surfPlayer.uuid).getLatestIpAddress()

        if (latestLogged != null && latestLogged == currentIp) {
            return
        }

        surfPlayerIpAddressHistoryRepository.addIpAddressToHistory(
            surfPlayer.uuid,
            IpAddressHistoryEntry(
                address = currentIp,
                lastSeen = OffsetDateTime.now()
            )
        )
    }


    override suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory =
        surfPlayerIpAddressHistoryRepository.getIpAddressHistory(uuid)
}