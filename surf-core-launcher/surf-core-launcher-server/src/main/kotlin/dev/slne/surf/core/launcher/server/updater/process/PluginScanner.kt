package dev.slne.surf.core.launcher.server.updater.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

class PluginScanner(
    private val pluginsPath: Path,
    private val ignoredPluginIds: Set<String> = emptySet(),
    private val warning: (String) -> Unit = {},
    private val diagnostic: (String) -> Unit = {}
) {
    private val jsonMapper: ObjectMapper = jacksonObjectMapper()
    private val yaml = Yaml(SafeConstructor(LoaderOptions()))

    suspend fun findPlugins(): List<UpdatablePlugin> = withContext(Dispatchers.IO) {
        if (!Files.isDirectory(pluginsPath)) {
            return@withContext emptyList()
        }

        val candidates = Files.list(pluginsPath).use { stream ->
            stream
                .filter(Files::isRegularFile)
                .filter(::hasJarExtension)
                .filter { it.fileName.toString().startsWith("surf-", ignoreCase = true) }
                .sorted()
                .toList()
        }
        val scannedPlugins = buildList {
            candidates.forEach { jarPath ->
                coroutineContext.ensureActive()
                readPlugin(jarPath)?.let(::add)
            }
        }.filterNot { plugin -> ignoredPluginIds.any { it.equals(plugin.name, ignoreCase = true) } }

        val duplicates = scannedPlugins
            .groupBy { it.name.lowercase() }
            .filterValues { it.size > 1 }
        duplicates.forEach { (pluginId, plugins) ->
            warning(
                "Duplicate plugin id $pluginId in " +
                        plugins.joinToString { it.jarPath.toString() } +
                        "; skipping all duplicates"
            )
        }

        scannedPlugins.filter { it.name.lowercase() !in duplicates }
    }

    private fun readPlugin(jarPath: Path): UpdatablePlugin? = try {
        JarFile(jarPath.toFile()).use { jar ->
            val descriptor = DESCRIPTOR_PRIORITY.firstNotNullOfOrNull(jar::getJarEntry)
                ?: error("no supported plugin descriptor found")

            jar.getInputStream(descriptor).use { input ->
                val (name, version) = when (descriptor.name) {
                    VELOCITY_DESCRIPTOR -> {
                        val data = jsonMapper.readValue(input, VelocityDescriptor::class.java)
                        data.id to data.version
                    }

                    else -> {
                        val data = yaml.load<Map<String, Any?>>(input)
                            ?: error("empty YAML descriptor")
                        data["name"]?.toString() to data["version"]?.toString()
                    }
                }

                UpdatablePlugin(
                    name = name?.takeIf(String::isNotBlank)
                        ?: error("descriptor has no plugin id/name"),
                    currentVersion = version?.takeIf(String::isNotBlank)
                        ?: error("descriptor has no version"),
                    jarPath = jarPath
                )
            }
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        diagnostic(
            "Could not scan $jarPath: " +
                    (exception.message ?: exception::class.simpleName.orEmpty())
        )
        null
    }

    private fun hasJarExtension(path: Path) = path.fileName
        .toString()
        .substringAfterLast('.', missingDelimiterValue = "")
        .equals("jar", ignoreCase = true)

    private data class VelocityDescriptor(
        val id: String? = null,
        val version: String? = null
    )

    companion object {
        private const val VELOCITY_DESCRIPTOR = "velocity-plugin.json"
        private val DESCRIPTOR_PRIORITY = listOf(
            "paper-plugin.yml",
            "plugin.yml",
            VELOCITY_DESCRIPTOR
        )
    }
}
