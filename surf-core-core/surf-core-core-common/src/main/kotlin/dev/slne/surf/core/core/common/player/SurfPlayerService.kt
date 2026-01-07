package dev.slne.surf.core.core.common.player

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

val surfPlayerService = requiredService<SurfPlayerService>()

interface SurfPlayerService {
    val players: ObjectSet<SurfPlayer>

    fun findPlayerByName(name: String): SurfPlayer?
    fun findPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun loadPlayerByName(name: String): SurfPlayer?
    suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun getOrLoadPlayerByName(name: String): SurfPlayer?
    suspend fun getOrLoadPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun getOrLoadOrCreatePlayerByUuid(uuid: UUID): SurfPlayer

    suspend fun savePlayer(player: SurfPlayer)

    fun clearPlayers()
    fun cachePlayer(player: SurfPlayer)
    fun invalidatePlayer(uuid: UUID)
}