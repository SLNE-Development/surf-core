package dev.slne.surf.core.launcher.server.updater.github

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class GitHubClient(private val token: String?) {
    private val mapper = jacksonObjectMapper()

    fun fetchLatestRelease(plugin: UpdatablePlugin): Map<String, Any>? = runCatching {
        val connection = openConnection(plugin.latestReleaseUrl, "application/vnd.github+json")
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        connection.connect()

        if (connection.responseCode != 200) {
            return null
        }

        connection.inputStream.use { input ->
            mapper.readValue(input, object : TypeReference<Map<String, Any>>() {})
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
}