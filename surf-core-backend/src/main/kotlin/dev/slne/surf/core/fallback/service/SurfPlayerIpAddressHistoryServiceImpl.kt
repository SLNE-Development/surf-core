package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.history.IpAddressHistory
import dev.slne.surf.core.api.common.player.history.entry.IpAddressHistoryEntry
import dev.slne.surf.core.core.common.player.history.SurfPlayerIpAddressHistoryService
import dev.slne.surf.core.fallback.repository.surfPlayerIpAddressHistoryRepository
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfPlayerIpAddressHistoryService::class)
class SurfPlayerIpAddressHistoryServiceImpl : SurfPlayerIpAddressHistoryService, Services.Fallback {
    override suspend fun addIpAddressToHistory(
        uuid: UUID,
        historyEntry: IpAddressHistoryEntry
    ) {
        surfPlayerIpAddressHistoryRepository.addIpAddressToHistory(uuid, historyEntry)
    }

    override suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory =
        surfPlayerIpAddressHistoryRepository.getIpAddressHistory(uuid)
}