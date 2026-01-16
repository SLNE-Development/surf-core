package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.surfapi.core.api.util.requiredService
import java.util.*

val surfPlayerNameHistoryService = requiredService<SurfPlayerNameHistoryService>()

interface SurfPlayerNameHistoryService {
    suspend fun handleNewName(surfPlayer: SurfPlayer)
    suspend fun getNameHistory(uuid: UUID): NameHistory
}