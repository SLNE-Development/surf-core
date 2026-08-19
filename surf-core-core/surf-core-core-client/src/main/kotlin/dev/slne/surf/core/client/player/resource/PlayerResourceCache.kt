package dev.slne.surf.core.client.player.resource

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.resource.PlayerResourceService
import java.util.*
import kotlin.time.Duration.Companion.days

object PlayerResourceCache {
    private val rpc by lazy {
        ClientCoreInstance.rabbitApi.createRpcService<PlayerResourceService>()
    }

    private val textureCacheByUuid =
        Caffeine.newBuilder().expireAfterWrite(1.days)
            .asLoadingCache<UUID, PlayerResource?> { uuid ->
                rpc.findPlayerResourceByUuid(uuid)
            }

    private val textureCacheByName =
        Caffeine.newBuilder().expireAfterWrite(1.days)
            .asLoadingCache<String, PlayerResource?> { username ->
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