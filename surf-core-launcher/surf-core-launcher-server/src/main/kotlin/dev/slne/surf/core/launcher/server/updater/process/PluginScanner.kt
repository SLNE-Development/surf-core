package dev.slne.surf.core.launcher.server.updater.process

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.extension
import kotlin.io.path.name

class PluginScanner(private val pluginsPath: Path) {
    private val yaml = Yaml()

    suspend fun findPlugins(): List<UpdatablePlugin> = withContext(Dispatchers.IO) {
        if (!Files.exists(pluginsPath)) {
            return@withContext emptyList()
        }

        Files.list(pluginsPath).use { stream ->
            stream
                .filter { it.extension == "jar" }
                .filter { it.name.startsWith("surf-") }
                .toList()
                .mapNotNull { readPlugin(it) }
                .filter { it.name !in CoreLauncher.config.autoUpdateIgnoredPlugins }
                .toList()
        }
    }

    private fun readPlugin(jarPath: Path): UpdatablePlugin? = runCatching {
        JarFile(jarPath.toFile()).use { jar ->
            val entry = jar.entries().asSequence().firstOrNull {
                it.name == "velocity-plugin.json" ||
                        it.name == "paper-plugin.yml" ||
                        it.name == "plugin.yml"
            } ?: return null

            jar.getInputStream(entry).use { input ->
                val data: Map<String, Any> = when (entry.name) {
                    "velocity-plugin.json" -> jacksonObjectMapper().readValue(
                        input,
                        Map::class.java
                    ) as Map<String, Any>

                    else -> yaml.load(input) ?: return null
                }

                val version = data["version"]?.toString() ?: return null

                val name = when (entry.name) {
                    "velocity-plugin.json" -> data["id"]?.toString()
                    else -> data["name"]?.toString()
                } ?: return null

                UpdatablePlugin(
                    name = name,
                    currentVersion = version,
                    jarPath = jarPath
                )
            }
        }
    }.getOrNull()
}