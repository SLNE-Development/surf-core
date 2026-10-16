package dev.slne.surf.core.core.common.redis.watcher

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.*
import kotlin.time.Duration.Companion.seconds

object PlayerJoinCheckResultWatcher {
    private val pendingRequests = Caffeine.newBuilder()
        .expireAfterWrite(30.seconds)
        .scheduler(Scheduler.systemScheduler())
        .evictionListener<UUID, CompletableDeferred<SurfJoinCheckResult>> { _, deferred, cause ->
            if (cause.wasEvicted() && deferred != null && !deferred.isCompleted) {
                deferred.complete(SurfJoinCheckResult.TIMEOUT)
            }
        }
        .build<UUID, CompletableDeferred<SurfJoinCheckResult>>()

    fun watch(requestId: UUID): Deferred<SurfJoinCheckResult> {
        return pendingRequests.get(requestId) { CompletableDeferred() }
    }

    fun complete(requestId: UUID, result: SurfJoinCheckResult) {
        val deferred = pendingRequests.asMap().remove(requestId)
        deferred?.complete(result)
    }
}
