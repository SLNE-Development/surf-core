package dev.slne.surf.core.launcher.server.updater.cooldown

import dev.slne.surf.core.launcher.server.updater.io.moveReplacingAtomically
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class UpdateCooldownTracker(
    private val persistencePath: Path,
    private val cooldownMs: Long = TimeUnit.MINUTES.toMillis(10),
    private val wallClock: () -> Long = System::currentTimeMillis,
    private val monotonicClock: () -> Long = System::nanoTime,
    private val diagnostic: (String) -> Unit = {}
) {
    private val lastUpdatedWallClock = ConcurrentHashMap<String, Long>()
    private val lastUpdatedMonotonic = ConcurrentHashMap<String, Long>()
    private val persistenceMutex = Mutex()
    private val cooldownNanos = TimeUnit.MILLISECONDS.toNanos(cooldownMs)

    init {
        require(cooldownMs > 0) { "Cooldown must be positive" }
        load()
        removeExpired()
    }

    fun isOnCooldown(pluginName: String): Boolean = remainingMillis(pluginName) > 0

    fun remainingSeconds(pluginName: String): Long {
        val remaining = remainingMillis(pluginName)
        return if (remaining == 0L) 0 else (remaining + 999L) / 1000L
    }

    fun markUpdated(pluginName: String) {
        lastUpdatedWallClock[pluginName] = wallClock()
        lastUpdatedMonotonic[pluginName] = monotonicClock()
    }

    fun removeExpired() {
        lastUpdatedWallClock.keys.forEach(::remainingMillis)
    }

    suspend fun persist() {
        persistenceMutex.withLock {
            withContext(Dispatchers.IO) {
                removeExpired()
                val parent = persistencePath.toAbsolutePath().parent
                Files.createDirectories(parent)
                val temporaryFile = Files.createTempFile(parent, ".last-updates-", ".tmp")

                try {
                    val content = lastUpdatedWallClock.entries
                        .sortedBy(Map.Entry<String, Long>::key)
                        .joinToString("\n") { (name, timestamp) -> "$name=$timestamp" }
                    Files.writeString(
                        temporaryFile,
                        content,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                    moveReplacingAtomically(temporaryFile, persistencePath.toAbsolutePath())
                } finally {
                    Files.deleteIfExists(temporaryFile)
                }
            }
        }
    }

    private fun remainingMillis(pluginName: String): Long {
        val wallTimestamp = lastUpdatedWallClock[pluginName] ?: return 0
        val monotonicTimestamp = lastUpdatedMonotonic[pluginName]
        val remaining = if (monotonicTimestamp != null) {
            val elapsedNanos = (monotonicClock() - monotonicTimestamp).coerceAtLeast(0)
            val remainingNanos = (cooldownNanos - elapsedNanos).coerceAtLeast(0)
            TimeUnit.NANOSECONDS.toMillis(remainingNanos)
        } else {
            val elapsedMs = (wallClock() - wallTimestamp).coerceAtLeast(0)
            (cooldownMs - elapsedMs).coerceAtLeast(0)
        }

        if (remaining == 0L) {
            lastUpdatedWallClock.remove(pluginName, wallTimestamp)
            if (monotonicTimestamp != null) {
                lastUpdatedMonotonic.remove(pluginName, monotonicTimestamp)
            }
        }
        return remaining
    }

    private fun load() {
        if (!Files.isRegularFile(persistencePath)) {
            return
        }

        Files.readAllLines(persistencePath).forEachIndexed { index, line ->
            val parts = line.split('=', limit = 2)
            val name = parts.getOrNull(0)?.trim().orEmpty()
            val timestamp = parts.getOrNull(1)?.trim()?.toLongOrNull()
            if (name.isBlank() || timestamp == null) {
                diagnostic("Ignoring malformed cooldown entry on line ${index + 1}")
                return@forEachIndexed
            }
            lastUpdatedWallClock[name] = timestamp
        }
    }
}
