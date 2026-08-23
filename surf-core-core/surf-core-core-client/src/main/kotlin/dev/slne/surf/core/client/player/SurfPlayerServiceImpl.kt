package dev.slne.surf.core.client.player

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.mutableObject2IntMapOf
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByNameRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByUuidRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.save.SaveSurfPlayerRequestPacket
import it.unimi.dsi.fastutil.objects.Object2IntMap
import java.util.*

@AutoService(SurfPlayerService::class)
class SurfPlayerServiceImpl : SurfPlayerService {
    private val _players =
        CoreInstance.redisApi.createSyncMap<UUID, SurfPlayer>("surf-core:surf-players")
    override val players get() = _players.snapshot().values.toObjectSet()

    override fun findPlayerByName(name: String): SurfPlayer? {
        for (player in _players.snapshot().values) {
            if (player.lastKnownName.equals(name, ignoreCase = true)) {
                return player
            }
        }

        return null
    }

    override fun findPlayerByUuid(uuid: UUID) = _players[uuid]

    override suspend fun loadPlayerByName(name: String): SurfPlayer? {
        return ClientCoreInstance.rabbitApi.sendRequest(LoadPlayerByNameRequestPacket(name)).player
    }

    override suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer? {
        return ClientCoreInstance.rabbitApi.sendRequest(LoadPlayerByUuidRequestPacket(uuid)).player
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
        ClientCoreInstance.rabbitApi.sendRequest(
            SaveSurfPlayerRequestPacket(
                uuid = player.uuid,
                name = player.lastKnownName,
                firstSeen = player.firstSeen,
                lastSeen = player.lastSeen,
                latestServer = player.currentServerName,
                latestProxy = player.currentProxyName
            )
        )
    }

    override fun playerCountsByServer(): Object2IntMap<String> =
        countPlayersBy(SurfPlayer::currentServerName)

    override fun playerCountsByProxy(): Object2IntMap<String> =
        countPlayersBy(SurfPlayer::currentProxyName)

    private fun countPlayersBy(selector: (SurfPlayer) -> String?): Object2IntMap<String> {
        val counts = mutableObject2IntMapOf<String>()

        for (player in _players.snapshot().values) {
            val key = selector(player) ?: continue
            counts.addTo(key, 1)
        }

        return counts
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
