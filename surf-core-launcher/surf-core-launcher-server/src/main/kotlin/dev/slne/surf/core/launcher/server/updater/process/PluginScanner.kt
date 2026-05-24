package dev.slne.surf.core.launcher.server.updater.process

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
                .toList()
        }
    }

    private fun readPlugin(jarPath: Path): UpdatablePlugin? = runCatching {
        JarFile(jarPath.toFile()).use { jar ->
            val entry = jar.entries().asSequence().firstOrNull {
                it.name == "velocity-plugin.yml" ||
                        it.name == "paper-plugin.yml" ||
                        it.name == "plugin.yml"
            } ?: return null

            jar.getInputStream(entry).use { input ->
                val data = yaml.load<Map<String, Any>>(input) ?: return null
                val version = data["version"]?.toString() ?: return null

                val name = when (entry.name) {
                    "velocity-plugin.yml" -> data["id"]?.toString()
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