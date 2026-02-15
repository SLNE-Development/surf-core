package dev.slne.surf.core.core.common.redis.watcher

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.redis.codec.UUIDCodec
import dev.slne.surf.redis.libs.redisson.api.RSetCacheReactive
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.reactor.awaitSingle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

object PlayerProxyConnectionResultWatcher {
    private val pendingRequests = Caffeine.newBuilder()
        .expireAfterWrite(20.seconds)
        .evictionListener<UUID, CompletableDeferred<SurfProxyServerConnectionResult>> { uuid, deferred, cause ->
            if (cause.wasEvicted() && deferred != null && !deferred.isCompleted) {
                deferred.complete(
                    SurfProxyServerConnectionResult(
                        SurfProxyServerConnectionResult.Status.ERR_UNKNOWN
                    )
                )
            }
        }
        .build<UUID, CompletableDeferred<SurfProxyServerConnectionResult>>()

    val transferringUuids: RSetCacheReactive<UUID> by lazy {
        redisApi.redissonReactive.getSetCache<UUID>(
            "surf-core:transferring-uuids",
            UUIDCodec.INSTANCE
        )
    }

    suspend fun cleanUp(playerUuid: UUID) {
        transferringUuids.remove(playerUuid).awaitSingle()
    }

    suspend fun watch(playerUuid: UUID): Deferred<SurfProxyServerConnectionResult> {
        if (transferringUuids.add(playerUuid, 15L, TimeUnit.SECONDS).awaitSingle()) {
            return pendingRequests.get(playerUuid) { CompletableDeferred() }
        }

        cleanUp(playerUuid)

        return CompletableDeferred(
            SurfProxyServerConnectionResult(
                SurfProxyServerConnectionResult.Status.ALREADY_TRANSFERRING
            )
        )
    }

    fun complete(playerUuid: UUID, result: SurfProxyServerConnectionResult) {
        val deferred = pendingRequests.asMap().remove(playerUuid)
        deferred?.complete(result)
    }
}