package dev.slne.surf.core.api.common.cache

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.SurfCoreApi
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

object OfflinePlayerNameCache {
    @Volatile
    private var sortedNames: List<String> = emptyList()
    private val CACHE_REFRESH_INTERVAL = 5.minutes

    suspend fun refresh() {
        sortedNames = SurfCoreApi.loadOfflinePlayerNameEntries().map { it.name }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    fun findByPrefix(prefix: String): List<String> {
        val lower = prefix.lowercase()
        val snapshot = sortedNames

        var lo = 0
        var hi = snapshot.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (snapshot[mid].lowercase() < lower) lo = mid + 1 else hi = mid
        }

        return buildList {
            var i = lo
            while (i < snapshot.size && snapshot[i].lowercase().startsWith(lower)) {
                add(snapshot[i])
                i++
            }
        }
    }

    fun startPulling(instanceScope: CoroutineScope) = instanceScope.launch(Dispatchers.IO) {
        while (isActive) {
            refresh()
            delay(CACHE_REFRESH_INTERVAL)
        }
    }

    @Serializable
    data class Entry(
        val uuid: SerializableUUID,
        val name: String
    )
}