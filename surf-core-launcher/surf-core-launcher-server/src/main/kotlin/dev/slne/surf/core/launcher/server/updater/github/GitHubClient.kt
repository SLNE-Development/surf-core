package dev.slne.surf.core.launcher.server.updater.github

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private const val CORE_REPOSITORY =
    "https://api.github.com/repos/SLNE-Development/surf-core/releases/latest"

class GitHubClient(private val token: String?) {
    private val mapper = jacksonObjectMapper()

    suspend fun fetchLatestRelease(url: String): Map<String, Any>? = runCatching {
        withContext(Dispatchers.IO) {
            val connection = openConnection(url, "application/vnd.github+json")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            connection.connect()

            if (connection.responseCode != 200) {
                println("$LOG_PREFIX (GitHubClient) Failed to fetch latest release from $url, response code: ${connection.responseCode}")
                return@withContext null
            }

            connection.inputStream.use {
                mapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
            }
        }
    }.getOrNull()

    fun downloadAsset(url: String, target: Path) {
        val connection = openConnection(url, "application/octet-stream")
        connection.connect()

        connection.inputStream.use { input ->
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun openConnection(url: String, accept: String): HttpURLConnection {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", accept)
        connection.setRequestProperty("User-Agent", "surf-core-launcher")
        connection.connectTimeout = 10_000
        connection.readTimeout = 20_000
        token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
        return connection
    }

    suspend fun checkForCoreLauncherUpdate() = withContext(Dispatchers.IO) {
        val currentVersion = LauncherConstants::class.java.`package`?.implementationVersion
            ?: return@withContext

        val release = fetchLatestRelease(CORE_REPOSITORY) ?: return@withContext
        val latestVersion = release["tag_name"] as? String ?: return@withContext

        if (!isNewerVersion(currentVersion, latestVersion)) {
            return@withContext
        }

        val asset = (release["assets"] as? List<*>)
            ?.filterIsInstance<Map<String, Any>>()
            ?.firstOrNull {
                (it["name"] as? String)
                    ?.startsWith("surf-core-launcher-server-") == true
            }

        val userUrl = asset?.get("html_url") as? String
            ?: release["html_url"] as? String
            ?: return@withContext

        println("$LOG_PREFIX " + "*".repeat(80))
        println("$LOG_PREFIX There is a new version of CoreLauncher available (v$currentVersion -> $latestVersion).")
        println("$LOG_PREFIX Download here: $userUrl")
        println("$LOG_PREFIX " + "*".repeat(80))
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        fun normalize(version: String) = version
            .removePrefix("v")
            .substringBefore('-')
            .split('.')
            .map { it.toIntOrNull() ?: 0 }

        val currentParts = normalize(current)
        val latestParts = normalize(latest)

        val max = maxOf(currentParts.size, latestParts.size)

        for (i in 0 until max) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val latestPart = latestParts.getOrElse(i) { 0 }

            when {
                latestPart > currentPart -> return true
                latestPart < currentPart -> return false
            }
        }

        return false
    }
}