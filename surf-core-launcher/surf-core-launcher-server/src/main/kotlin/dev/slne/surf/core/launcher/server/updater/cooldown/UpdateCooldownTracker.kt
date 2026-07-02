package dev.slne.surf.core.launcher.server.updater.cooldown

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class UpdateCooldownTracker(private val persistencePath: Path) {
    private val lastUpdated = ConcurrentHashMap<String, Long>()
    private val cooldownMs = TimeUnit.MINUTES.toMillis(10)

    init {
        load()
    }

    fun isOnCooldown(pluginName: String): Boolean {
        val lastUpdate = lastUpdated[pluginName] ?: return false
        return System.currentTimeMillis() - lastUpdate < cooldownMs
    }

    fun remainingSeconds(pluginName: String): Long {
        val lastUpdate = lastUpdated[pluginName] ?: return 0
        return (cooldownMs - (System.currentTimeMillis() - lastUpdate)) / 1000
    }

    fun markUpdated(pluginName: String) {
        lastUpdated[pluginName] = System.currentTimeMillis()
        save()
    }

    private fun load() {
        if (!Files.exists(persistencePath)) {
            return
        }

        Files.readAllLines(persistencePath).forEach { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size != 2) return@forEach
            lastUpdated[parts[0].trim()] = parts[1].trim().toLongOrNull() ?: return@forEach
        }
    }

    private fun save() {
        Files.writeString(
            persistencePath,
            lastUpdated.entries.joinToString("\n") { (name, ts) -> "$name=$ts" },
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}