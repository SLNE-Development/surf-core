package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.fallback.repository.surfPlayerRepository
import dev.slne.surf.redis.sync.map.SyncMap
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import net.kyori.adventure.util.Services
import java.util.*

typealias UuidString = String

@AutoService(SurfPlayerService::class)
class SurfPlayerServiceImpl : SurfPlayerService, Services.Fallback {
    lateinit var globalPlayers: SyncMap<UuidString, SurfPlayer>
    override val players get() = globalPlayers.snapshot().values.toObjectSet()

    override fun findPlayerByName(name: String) =
        players.firstOrNull { it.lastKnownName.equals(name, ignoreCase = true) }

    override fun findPlayerByUuid(uuid: UUID) = players.firstOrNull { it.uuid == uuid }
    override fun init() {
        globalPlayers = redisApi.createSyncMap("surf-core:players")
    }

    override suspend fun loadPlayerByName(name: String) =
        surfPlayerRepository.loadPlayerByName(name)

    override suspend fun loadPlayerByUuid(uuid: UUID) = surfPlayerRepository.loadPlayerByUuid(uuid)
    override suspend fun getOrLoadPlayerByName(name: String) =
        findPlayerByName(name) ?: loadPlayerByName(name)

    override suspend fun getOrLoadPlayerByUuid(uuid: UUID) =
        findPlayerByUuid(uuid) ?: loadPlayerByUuid(uuid)

    override suspend fun getOrLoadOrCreatePlayerByUuid(uuid: UUID) = getOrLoadPlayerByUuid(uuid)
        ?: SurfPlayer(
            uuid = uuid,
            lastKnownName = null,
            firstSeen = null,
            lastSeen = null
        )

    override suspend fun savePlayer(player: SurfPlayer) = surfPlayerRepository.savePlayer(player)
    override fun clearPlayers() {
        globalPlayers.clear()
    }

    override fun cachePlayer(player: SurfPlayer) {
        globalPlayers.put(player.uuid.toString(), player)
    }

    override fun invalidatePlayer(uuid: UUID) {
        globalPlayers.remove(uuid.toString())
    }
}