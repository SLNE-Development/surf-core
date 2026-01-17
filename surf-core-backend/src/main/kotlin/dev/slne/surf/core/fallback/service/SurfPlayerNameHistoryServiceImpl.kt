package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.core.api.common.player.history.name.NameHistoryEntry
import dev.slne.surf.core.core.common.player.history.SurfPlayerNameHistoryService
import dev.slne.surf.core.fallback.repository.surfPlayerNameHistoryRepository
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*

@AutoService(SurfPlayerNameHistoryService::class)
class SurfPlayerNameHistoryServiceImpl : SurfPlayerNameHistoryService, Services.Fallback {
    override suspend fun handleNewName(surfPlayer: SurfPlayer) {
        val currentName = surfPlayer.lastKnownName ?: return
        val latestLogged = getNameHistory(surfPlayer.uuid).getCurrentName()

        if (latestLogged != null && latestLogged == currentName) {
            return
        }

        surfPlayerNameHistoryRepository.addNameToHistory(
            surfPlayer.uuid, NameHistoryEntry(
                name = currentName,
                lastSeen = OffsetDateTime.now()
            )
        )
    }

    override suspend fun getNameHistory(uuid: UUID): NameHistory =
        surfPlayerNameHistoryRepository.getNameHistory(uuid)
}