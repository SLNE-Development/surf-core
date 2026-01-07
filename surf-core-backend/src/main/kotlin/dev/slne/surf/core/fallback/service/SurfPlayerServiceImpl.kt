package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.fallback.repository.surfPlayerRepository
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfPlayerService::class)
class SurfPlayerServiceImpl : SurfPlayerService, Services.Fallback {
    val globalPlayers = redisApi.createSyncMap<UUID, SurfPlayer>("surf-core:players-new")
    override val players get() = globalPlayers.snapshot().values.toObjectSet()

    override fun findPlayerByName(name: String) =
        players.firstOrNull { it.lastKnownName.equals(name, ignoreCase = true) }

    override fun findPlayerByUuid(uuid: UUID) = globalPlayers.get(uuid)
    override fun init() {
    }

    override suspend fun loadPlayerByName(name: String) =
        surfPlayerRepository.loadPlayerByName(name)

    override suspend fun loadPlayerByUuid(uuid: UUID) = surfPlayerRepository.loadPlayerByUuid(uuid)
    override suspend fun getOrLoadPlayerByName(name: String) =
        findPlayerByName(name) ?: loadPlayerByName(name)

    override suspend fun getOrLoadPlayerByUuid(uuid: UUID): SurfPlayer? {
        return findPlayerByUuid(uuid) ?: loadPlayerByUuid(uuid)
    }

    override suspend fun getOrLoadOrCreatePlayerByUuid(uuid: UUID): SurfPlayer {
        return getOrLoadPlayerByUuid(uuid) ?: SurfPlayer(
            uuid = uuid,
            lastKnownName = null,
            firstSeen = null,
            lastSeen = null
        )
    }

    override suspend fun savePlayer(player: SurfPlayer) = surfPlayerRepository.savePlayer(player)
    override fun clearPlayers() {
        globalPlayers.clear()
    }

    override fun cachePlayer(player: SurfPlayer) {
        globalPlayers.put(player.uuid, player)
        println(
            "Cached player: ${player.uuid}, now: ${
                players.map { it.lastKnownName }
            }"
        )
    }

    override fun invalidatePlayer(uuid: UUID) {
        globalPlayers.remove(uuid)
        println(
            "Invalidated player: $uuid, now: ${
                players.map { it.lastKnownName }
            }"
        )
    }
}