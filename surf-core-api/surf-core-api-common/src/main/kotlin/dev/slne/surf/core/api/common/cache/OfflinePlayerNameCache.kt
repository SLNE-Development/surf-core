package dev.slne.surf.core.api.common.cache

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.SurfCoreApi
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

object OfflinePlayerNameCache {
    private val logger = logger()

    @Volatile
    private var sortedNames: List<String> = emptyList()
    private val CACHE_REFRESH_INTERVAL = 5.minutes

    suspend fun refresh() {
        sortedNames = SurfCoreApi.loadOfflinePlayerNameEntries().map { it.name }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    /**
     * Names starting with [prefix], case-insensitively, in cache order and capped at [limit].
     */
    @OptIn(ExperimentalVersionOverloading::class)
    fun findByPrefix(
        prefix: String,
        @IntroducedAt("2.5.0") limit: Int = Int.MAX_VALUE
    ): List<String> {
        if (limit <= 0) return emptyList()

        val snapshot = sortedNames

        var lo = 0
        var hi = snapshot.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (String.CASE_INSENSITIVE_ORDER.compare(snapshot[mid], prefix) < 0) {
                lo = mid + 1
            } else {
                hi = mid
            }
        }

        val matches = ObjectArrayList<String>(minOf(limit, snapshot.size - lo).coerceAtLeast(0))
        var i = lo
        while (i < snapshot.size && matches.size < limit) {
            val candidate = snapshot[i]
            if (!candidate.startsWith(prefix, ignoreCase = true)) break
            matches.add(candidate)
            i++
        }

        return matches
    }

    fun startPulling(instanceScope: CoroutineScope) = instanceScope.launch(Dispatchers.IO) {
        while (isActive) {
            runCatching {
                refresh()
            }.onFailure {
                logger.atFine().log("Failed to refresh offline player name cache: ${it.message}")
            }
            delay(CACHE_REFRESH_INTERVAL)
        }
    }

    @Serializable
    data class Entry(
        val uuid: SerializableUUID,
        val name: String
    )
}