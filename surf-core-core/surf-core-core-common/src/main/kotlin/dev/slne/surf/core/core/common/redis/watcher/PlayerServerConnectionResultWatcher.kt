package dev.slne.surf.core.core.common.redis.watcher

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.*
import kotlin.time.Duration.Companion.minutes

object PlayerServerConnectionResultWatcher {
    private val pendingRequests = Caffeine.newBuilder()
        .expireAfterWrite(10.minutes)
        .scheduler(Scheduler.systemScheduler()) // Make sure the eviction listener will be called
        .evictionListener<UUID, CompletableDeferred<SurfServerConnectResult>> { uuid, deferred, cause ->
            if (cause.wasEvicted() && deferred != null && !deferred.isCompleted) {
                deferred.complete(
                    SurfServerConnectResult(
                        SurfServerConnectResult.Status.UNKNOWN_ERROR,
                        null
                    )
                )
            }
        }
        .build<UUID, CompletableDeferred<SurfServerConnectResult>>()

    fun watch(requestId: UUID): Deferred<SurfServerConnectResult> {
        return pendingRequests.get(requestId) { CompletableDeferred() }
    }

    fun complete(requestId: UUID, result: SurfServerConnectResult) {
        val deferred = pendingRequests.asMap().remove(requestId)
        deferred?.complete(result)
    }
}