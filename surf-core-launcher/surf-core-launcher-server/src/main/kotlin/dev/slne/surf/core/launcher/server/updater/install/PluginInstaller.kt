package dev.slne.surf.core.launcher.server.updater.install

import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import dev.slne.surf.core.launcher.server.updater.asset.ReleaseAsset
import dev.slne.surf.core.launcher.server.updater.io.moveReplacingAtomically
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.Serial
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.jar.JarFile

data class InstalledPlugin(
    val installedPath: Path,
    val backupPath: Path?,
    val backupCleanupFailure: Throwable? = null
)

class PluginInstallationException(
    val operation: String,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -6356641373606594763L
    }
}

class PluginInstaller(
    private val pluginsPath: Path,
    private val backupPath: Path = pluginsPath.resolve(".old"),
    private val backupRetention: Int = DEFAULT_BACKUP_RETENTION,
    private val clock: Clock = Clock.systemUTC(),
    private val moveReplacing: (Path, Path) -> Unit = ::moveReplacingAtomically
) {
    init {
        require(backupRetention >= 1) { "At least one backup must be retained" }
    }

    suspend fun install(
        plugin: UpdatablePlugin,
        asset: ReleaseAsset,
        download: suspend (ReleaseAsset, Path) -> Unit
    ): InstalledPlugin {
        val target = resolveTarget(asset.name)
        val temporaryFile = withContext(Dispatchers.IO) {
            Files.createDirectories(pluginsPath)
            Files.createTempFile(pluginsPath, ".${safeName(plugin.name)}-", ".download")
        }

        try {
            try {
                download(asset, temporaryFile)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                throw PluginInstallationException(
                    operation = "download",
                    message = "download failed for ${asset.name}: ${exception.message ?: exception::class.simpleName}",
                    cause = exception
                )
            }

            return withContext(Dispatchers.IO) {
                validateDownload(temporaryFile, asset)
                installValidatedFile(plugin, temporaryFile, target)
            }
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                Files.deleteIfExists(temporaryFile)
            }
        }
    }

    private fun installValidatedFile(
        plugin: UpdatablePlugin,
        temporaryFile: Path,
        target: Path
    ): InstalledPlugin {
        val installedPath = plugin.jarPath.toAbsolutePath().normalize()
        val normalizedTarget = target.toAbsolutePath().normalize()
        if (normalizedTarget != installedPath && Files.exists(normalizedTarget)) {
            throw PluginInstallationException(
                operation = "install",
                message = "refusing to overwrite existing file $normalizedTarget"
            )
        }

        Files.createDirectories(backupPath)
        val backup = if (Files.isRegularFile(installedPath)) {
            uniqueBackupPath(plugin)
        } else {
            null
        }
        var backupMaintenanceFailure: Throwable? = null

        if (backup != null) {
            try {
                moveReplacing(installedPath, backup)
            } catch (exception: Exception) {
                val failure = PluginInstallationException(
                    operation = "backup",
                    message = "failed to back up ${plugin.name}",
                    cause = exception
                )
                if (Files.exists(backup) && !Files.exists(installedPath)) {
                    try {
                        moveReplacing(backup, installedPath)
                    } catch (restorationFailure: Exception) {
                        failure.addSuppressed(restorationFailure)
                    }
                }
                throw failure
            }
            try {
                Files.setLastModifiedTime(backup, FileTime.from(Instant.now(clock)))
            } catch (exception: Exception) {
                backupMaintenanceFailure = exception
            }
        }

        try {
            moveReplacing(temporaryFile, normalizedTarget)
        } catch (exception: Exception) {
            val failure = PluginInstallationException(
                operation = "install",
                message = "failed to install ${plugin.name}",
                cause = exception
            )

            if (backup != null) {
                if (normalizedTarget != installedPath) {
                    try {
                        Files.deleteIfExists(normalizedTarget)
                    } catch (cleanupFailure: Exception) {
                        failure.addSuppressed(cleanupFailure)
                    }
                }

                try {
                    moveReplacing(backup, installedPath)
                } catch (restorationFailure: Exception) {
                    failure.addSuppressed(restorationFailure)
                }
            }

            throw failure
        }

        val cleanupFailure = try {
            pruneOldBackups(plugin)
            null
        } catch (exception: Exception) {
            exception
        }
        if (backupMaintenanceFailure != null && cleanupFailure != null) {
            backupMaintenanceFailure.addSuppressed(cleanupFailure)
        }
        return InstalledPlugin(
            normalizedTarget,
            backup,
            backupMaintenanceFailure ?: cleanupFailure
        )
    }

    private fun validateDownload(path: Path, asset: ReleaseAsset) {
        fun invalid(message: String, cause: Throwable? = null): Nothing {
            throw PluginInstallationException("validation", message, cause)
        }

        if (!Files.isRegularFile(path)) {
            invalid("downloaded file does not exist")
        }
        if (!Files.isReadable(path)) {
            invalid("downloaded file is not readable")
        }

        val actualSize = Files.size(path)
        if (actualSize <= 0) {
            invalid("downloaded file is empty")
        }
        if (asset.size != null && actualSize != asset.size) {
            invalid("downloaded size $actualSize does not match expected size ${asset.size}")
        }

        try {
            JarFile(path.toFile()).use { it.entries().hasMoreElements() }
        } catch (exception: Exception) {
            invalid("downloaded file is not a readable JAR", exception)
        }
    }

    private fun resolveTarget(assetName: String): Path {
        if (assetName.isBlank() || '/' in assetName || '\\' in assetName) {
            throw PluginInstallationException("validation", "invalid asset name")
        }

        val target = pluginsPath.resolve(assetName).toAbsolutePath().normalize()
        val normalizedPluginsPath = pluginsPath.toAbsolutePath().normalize()
        if (target.parent != normalizedPluginsPath) {
            throw PluginInstallationException("validation", "asset target leaves the plugins directory")
        }
        return target
    }

    private fun uniqueBackupPath(plugin: UpdatablePlugin): Path {
        val prefix = backupPrefix(plugin.name)
        val timestamp = BACKUP_TIMESTAMP_FORMATTER.format(Instant.now(clock))
        repeat(10) {
            val candidate = backupPath.resolve(
                "$prefix${safeName(plugin.currentVersion)}-$timestamp-${UUID.randomUUID()}.jar"
            )
            if (!Files.exists(candidate)) {
                return candidate
            }
        }
        throw PluginInstallationException("backup", "could not allocate a unique backup name")
    }

    private fun pruneOldBackups(plugin: UpdatablePlugin) {
        val prefix = backupPrefix(plugin.name)
        val backups = Files.list(backupPath).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .filter { it.fileName.toString().startsWith(prefix) }
                .sorted(
                    compareByDescending<Path> { Files.getLastModifiedTime(it).toMillis() }
                        .thenByDescending { it.fileName.toString() }
                )
                .toList()
        }

        backups.drop(backupRetention).forEach(Files::deleteIfExists)
    }

    private fun backupPrefix(pluginName: String) = "${safeName(pluginName)}--"

    private fun safeName(value: String) = value.replace(Regex("[^A-Za-z0-9._-]"), "_")

    companion object {
        const val DEFAULT_BACKUP_RETENTION = 3
        private val BACKUP_TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd-HHmmss-SSS")
            .withZone(ZoneOffset.UTC)
    }
}
