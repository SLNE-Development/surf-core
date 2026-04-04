package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.name.NameHistory
import java.util.*

private val service = requiredService<SurfPlayerNameHistoryService>()

interface SurfPlayerNameHistoryService {
    suspend fun handleNewName(surfPlayer: SurfPlayer)
    suspend fun getNameHistory(uuid: UUID): NameHistory

    companion object : SurfPlayerNameHistoryService by service {
        val INSTANCE get() = service
    }
}