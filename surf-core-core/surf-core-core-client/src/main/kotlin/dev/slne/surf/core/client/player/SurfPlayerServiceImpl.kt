package dev.slne.surf.core.client.player

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByNameRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByUuidRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.save.SaveSurfPlayerRequestPacket
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import java.util.*

@AutoService(SurfPlayerService::class)
class SurfPlayerServiceImpl : SurfPlayerService {
    private val _players =
        ClientLoader.redisApi.createSyncMap<UUID, SurfPlayer>("surf-core:surf-players")
    override val players get() = _players.snapshot().values.toObjectSet()

    override fun findPlayerByName(name: String) =
        players.firstOrNull { it.lastKnownName.equals(name, ignoreCase = true) }

    override fun findPlayerByUuid(uuid: UUID) = _players[uuid]

    override suspend fun loadPlayerByName(name: String): SurfPlayer? {
        return ClientLoader.rabbitApi.sendRequest(LoadPlayerByNameRequestPacket(name)).player
    }

    override suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer? {
        return ClientLoader.rabbitApi.sendRequest(LoadPlayerByUuidRequestPacket(uuid)).player
    }

    override suspend fun getOrLoadPlayerByName(name: String) =
        findPlayerByName(name) ?: loadPlayerByName(name)

    override suspend fun getOrLoadPlayerByUuid(uuid: UUID) =
        findPlayerByUuid(uuid) ?: loadPlayerByUuid(uuid)

    override suspend fun getOrLoadOrCreatePlayerByUuid(uuid: UUID) =
        getOrLoadPlayerByUuid(uuid) ?: SurfPlayer(
            uuid = uuid,
            lastKnownName = null,
            firstSeen = null,
            lastSeen = null,
            transferred = false
        )

    override suspend fun savePlayer(player: SurfPlayer) {
        ClientLoader.rabbitApi.sendRequest(SaveSurfPlayerRequestPacket(player))
    }

    override fun clearPlayers() {
        _players.clear()
    }

    override fun cachePlayer(player: SurfPlayer) {
        _players.put(player.uuid, player)
    }

    override fun invalidatePlayer(uuid: UUID) {
        _players.remove(uuid)
    }
}