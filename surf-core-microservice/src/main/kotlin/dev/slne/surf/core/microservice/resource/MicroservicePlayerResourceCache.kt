package dev.slne.surf.core.microservice.resource

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.microservice.resource.database.PlayerResourceRepository
import dev.slne.surf.core.microservice.resource.source.sources.MinecraftPlayerResourceSource
import java.util.*
import kotlin.time.Duration.Companion.days

object MicroservicePlayerResourceCache {
    private val textureCacheByUuid =
        Caffeine.newBuilder().expireAfterWrite(1.days)
            .asLoadingCache<UUID, PlayerResource?> { uuid ->
                PlayerResourceRepository.findPlayerResource(uuid)
                    ?: MinecraftPlayerResourceSource.findPlayerResource(uuid)
            }

    private val textureCacheByName =
        Caffeine.newBuilder().expireAfterWrite(1.days)
            .asLoadingCache<String, PlayerResource?> { username ->
                PlayerResourceRepository.findPlayerResource(username)
                    ?: MinecraftPlayerResourceSource.findPlayerResource(username)
            }

    suspend fun findPlayerResource(playerUuid: SerializableUUID) =
        textureCacheByUuid.get(playerUuid)

    suspend fun findPlayerResource(username: String): PlayerResource? =
        textureCacheByName.get(username)

    suspend fun batchPlayerResources(list: List<SerializableUUID>): List<PlayerResource> =
        textureCacheByUuid.getAll(list) { uuids ->
            val (resources, missing) = PlayerResourceRepository.batchPlayerResources(uuids.toList())

            val minecraftResources = missing.mapNotNull { uuid ->
                MinecraftPlayerResourceSource.findPlayerResource(uuid)
            }

            (resources + minecraftResources).associateBy { it.playerUuid }
        }.mapNotNull { it.value }
}