package dev.slne.surf.core.client.player.resource

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.resource.PlayerResourceService
import java.util.*

object PlayerResourceCache {
    private val rpc by lazy {
        ClientCoreInstance.rabbitApi.createRpcService<PlayerResourceService>()
    }

    private val textureCacheByUuid =
        Caffeine.newBuilder().asLoadingCache<UUID, PlayerResource?> { uuid ->
            rpc.findPlayerResourceByUuid(uuid)
        }

    private val textureCacheByName =
        Caffeine.newBuilder().asLoadingCache<String, PlayerResource?> { username ->
            rpc.findPlayerResourceByName(username)
        }

    suspend fun resourceFromUuid(playerUuid: SerializableUUID) =
        textureCacheByUuid.get(playerUuid)

    suspend fun resourceFromUsername(username: String): PlayerResource? =
        textureCacheByName.get(username)

    suspend fun batch(list: List<SerializableUUID>): List<PlayerResource> =
        textureCacheByUuid.getAll(list) { uuids ->
            rpc.batchPlayerResources(uuids.toList()).associateBy { it.playerUuid }
        }.mapNotNull { it.value }


    fun invalidateAll() {
        textureCacheByUuid.invalidateAll()
        textureCacheByName.invalidateAll()
    }
}