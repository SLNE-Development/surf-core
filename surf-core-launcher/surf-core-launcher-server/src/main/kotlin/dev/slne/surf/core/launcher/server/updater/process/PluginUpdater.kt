package dev.slne.surf.core.launcher.server.updater.process

import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import dev.slne.surf.core.launcher.server.updater.cooldown.UpdateCooldownTracker
import dev.slne.surf.core.launcher.server.updater.github.GitHubClient
import kotlinx.coroutines.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.measureTimeMillis

object PluginUpdater {
    private val pluginsPath = Path.of("plugins")
    private val oldPath = pluginsPath.resolve(".old")

    private val scanner = PluginScanner(pluginsPath)
    internal val gitHubClient = GitHubClient(CoreLauncher.config.personalAccessToken)
    private val cooldownTracker = UpdateCooldownTracker(pluginsPath.resolve(".last-updates"))

    suspend fun start() {
        val plugins = scanner.findPlugins()

        if (plugins.isEmpty()) {
            println("$LOG_PREFIX (Updater) No plugins found for update checking.")
            return
        }

        withContext(Dispatchers.IO) {
            if (!Files.exists(oldPath)) {
                Files.createDirectories(oldPath)
            }
        }

        println("$LOG_PREFIX (Updater) Checking plugin updates for ${plugins.size} plugins...")

        val duration = measureTimeMillis {
            coroutineScope {
                plugins.map { plugin ->
                    async(Dispatchers.IO) {
                        checkAndUpdate(plugin)
                    }
                }.awaitAll()
            }
        }

        println("$LOG_PREFIX (Updater) Update check completed in ${duration}ms")
    }


    private val assetMappings = mapOf("surf-paper-paper" to "surf-api-paper")

    private suspend fun checkAndUpdate(plugin: UpdatablePlugin) = withContext(Dispatchers.IO) {
        if (cooldownTracker.isOnCooldown(plugin.name)) {
            return@withContext
        }

        val release = gitHubClient.fetchLatestRelease(plugin.latestReleaseUrl)
        val latestVersion = release?.get("tag_name")?.toString()?.trimStart('v')

        if (latestVersion == null) {
            if (CoreLauncher.config.logGithubReleaseFetchFailures) {
                println("$LOG_PREFIX Missing, invalid or incomplete latest release for ${plugin.name} (${plugin.latestReleaseUrl})")
            }
            return@withContext
        }

        if (!isNewer(latestVersion, plugin.currentVersion)) {
            return@withContext
        }

        @Suppress("UNCHECKED_CAST")
        val assets = release["assets"] as? List<Map<String, Any>> ?: return@withContext

        val buildedPrefix = buildString {
            append("surf-")
            append(plugin.findPluginName(false))

            plugin.findPluginType()?.let {
                append("-")
                append(plugin.findPluginType())
            }
        }

        val mappedPrefix = assetMappings[buildedPrefix] ?: buildedPrefix

        val matchingAsset = assets.firstOrNull { asset ->
            val assetName = asset["name"]?.toString() ?: return@firstOrNull false

            assetName.endsWith(".jar") &&
                    assetName.startsWith(mappedPrefix)
        } ?: run {
            println("$LOG_PREFIX (Updater) No matching asset found for ${plugin.name}: $mappedPrefix in release $latestVersion")
            return@withContext
        }

        val downloadUrl = matchingAsset["browser_download_url"]?.toString() ?: return@withContext
        val assetName = matchingAsset["name"]?.toString() ?: return@withContext

        backupOldJar(plugin)

        gitHubClient.downloadAsset(downloadUrl, pluginsPath.resolve(assetName))
        cooldownTracker.markUpdated(plugin.name)

        println("$LOG_PREFIX (Updater) Updated ${plugin.name} from ${plugin.currentVersion} to $latestVersion")
    }

    private fun backupOldJar(plugin: UpdatablePlugin) {
        if (!Files.exists(plugin.jarPath)) {
            return
        }

        Files.move(
            plugin.jarPath,
            oldPath.resolve("${plugin.name}-${plugin.currentVersion}.jar"),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    private fun isNewer(latest: String, current: String): Boolean {
        fun split(v: String): Pair<List<Int>, String?> {
            val parts = v.split("-", limit = 2)
            val nums = parts[0].split(".").map { it.toIntOrNull() ?: 0 }
            val suffix = parts.getOrNull(1)?.uppercase()
            return nums to suffix
        }

        fun rank(suffix: String?): Int {
            if (suffix == null) return 3
            return when {
                suffix.contains("SNAPSHOT") -> 0
                suffix.contains("ALPHA") -> 1
                suffix.contains("BETA") -> 2
                else -> 1
            }
        }

        val (lNums, lSuffix) = split(latest)
        val (cNums, cSuffix) = split(current)

        for (i in 0 until maxOf(lNums.size, cNums.size)) {
            val diff = lNums.getOrElse(i) { 0 } - cNums.getOrElse(i) { 0 }
            if (diff != 0) return diff > 0
        }

        return rank(lSuffix) > rank(cSuffix)
    }
}