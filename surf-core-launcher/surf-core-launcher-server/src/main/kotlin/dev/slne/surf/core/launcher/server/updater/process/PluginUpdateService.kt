package dev.slne.surf.core.launcher.server.updater.process

import dev.slne.surf.core.launcher.server.updater.GitHubRepositoryCoordinates
import dev.slne.surf.core.launcher.server.updater.PluginRepositoryMappings
import dev.slne.surf.core.launcher.server.updater.PluginUpdateTarget
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import dev.slne.surf.core.launcher.server.updater.asset.AssetSelectionResult
import dev.slne.surf.core.launcher.server.updater.asset.PluginAssetSelector
import dev.slne.surf.core.launcher.server.updater.asset.ReleaseAsset
import dev.slne.surf.core.launcher.server.updater.cooldown.UpdateCooldownTracker
import dev.slne.surf.core.launcher.server.updater.github.GitHubAssetDownloadException
import dev.slne.surf.core.launcher.server.updater.github.LatestGitHubRelease
import dev.slne.surf.core.launcher.server.updater.github.LatestReleaseResult
import dev.slne.surf.core.launcher.server.updater.install.PluginInstallationException
import dev.slne.surf.core.launcher.server.updater.install.PluginInstaller
import dev.slne.surf.core.launcher.server.updater.version.Version
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.coroutineContext

class PluginUpdateService(
    private val cooldownTracker: UpdateCooldownTracker,
    private val installer: PluginInstaller,
    private val fetchLatestRelease: suspend (GitHubRepositoryCoordinates) -> LatestReleaseResult,
    private val downloadAsset: suspend (ReleaseAsset, java.nio.file.Path) -> Unit,
    maxConcurrency: Int = DEFAULT_MAX_CONCURRENCY,
    private val diagnostic: (String) -> Unit = {}
) {
    private val concurrency = Semaphore(maxConcurrency)

    init {
        require(maxConcurrency > 0) { "Update concurrency must be positive" }
    }

    suspend fun update(
        plugins: List<UpdatablePlugin>,
        onResult: (PluginUpdateResult) -> Unit = {}
    ): List<PluginUpdateResult> {
        val initialResults = mutableListOf<PluginUpdateResult>()
        val duplicates = plugins
            .groupBy { it.name.lowercase() }
            .filterValues { it.size > 1 }
            .keys
        val activePlugins = buildList {
            plugins.forEach { plugin ->
                val result = when {
                    plugin.name.lowercase() in duplicates -> PluginUpdateResult.Skipped(
                        plugin,
                        SkipReason.DUPLICATE_PLUGIN_ID,
                        "duplicate plugin id"
                    )

                    cooldownTracker.isOnCooldown(plugin.name) -> PluginUpdateResult.Skipped(
                        plugin,
                        SkipReason.COOLDOWN,
                        "cooldown has ${cooldownTracker.remainingSeconds(plugin.name)}s remaining"
                    )

                    Version.parse(plugin.currentVersion) == null -> PluginUpdateResult.Skipped(
                        plugin,
                        SkipReason.MALFORMED_CURRENT_VERSION,
                        "installed version '${plugin.currentVersion}' is malformed"
                    )

                    else -> null
                }

                if (result == null) {
                    add(
                        ActivePlugin(
                            target = PluginRepositoryMappings.resolve(plugin),
                            currentVersion = checkNotNull(Version.parse(plugin.currentVersion))
                        )
                    )
                } else {
                    initialResults += result
                    onResult(result)
                }
            }
        }

        val repositoryResults = supervisorScope {
            activePlugins
                .groupBy { it.target.repository }
                .map { (repository, repositoryPlugins) ->
                    async(Dispatchers.IO) {
                        concurrency.withPermit {
                            try {
                                processRepository(repository, repositoryPlugins, onResult)
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                repositoryPlugins.map { activePlugin ->
                                    PluginUpdateResult.Failed(
                                        plugin = activePlugin.target.plugin,
                                        repository = repository,
                                        operation = "repository update",
                                        message = exception.message ?: exception::class.simpleName.orEmpty(),
                                        cause = exception
                                    ).also(onResult)
                                }
                            }
                        }
                    }
                }
                .awaitAll()
                .flatten()
        }

        return initialResults + repositoryResults
    }

    private suspend fun processRepository(
        repository: GitHubRepositoryCoordinates,
        plugins: List<ActivePlugin>,
        onResult: (PluginUpdateResult) -> Unit
    ): List<PluginUpdateResult> {
        currentCoroutineContext().ensureActive()
        return when (val releaseResult = fetchLatestRelease(repository)) {
            is LatestReleaseResult.Found -> processRelease(releaseResult.value, plugins, onResult)
            is LatestReleaseResult.RepositoryUnavailable -> plugins.map { activePlugin ->
                PluginUpdateResult.Failed(
                    plugin = activePlugin.target.plugin,
                    repository = repository,
                    operation = "resolve repository",
                    status = 404,
                    message = "repository was not found or the token lacks access"
                ).also(onResult)
            }

            is LatestReleaseResult.NoPublishedRelease -> plugins.map { activePlugin ->
                PluginUpdateResult.Skipped(
                    plugin = activePlugin.target.plugin,
                    reason = SkipReason.NO_PUBLISHED_RELEASE,
                    message = "$repository has no published release"
                ).also(onResult)
            }

            is LatestReleaseResult.Failed -> plugins.map { activePlugin ->
                PluginUpdateResult.Failed(
                    plugin = activePlugin.target.plugin,
                    repository = repository,
                    operation = releaseResult.operation,
                    status = releaseResult.status,
                    message = releaseResult.message,
                    rateLimit = releaseResult.rateLimit
                ).also(onResult)
            }
        }
    }

    private suspend fun processRelease(
        release: LatestGitHubRelease,
        plugins: List<ActivePlugin>,
        onResult: (PluginUpdateResult) -> Unit
    ): List<PluginUpdateResult> {
        val latestVersion = Version.parse(release.tagName)
            ?: return plugins.map { activePlugin ->
                PluginUpdateResult.Skipped(
                    plugin = activePlugin.target.plugin,
                    reason = SkipReason.MALFORMED_RELEASE_VERSION,
                    message = "release tag '${release.tagName}' is malformed"
                ).also(onResult)
            }

        return plugins.map { activePlugin ->
            currentCoroutineContext().ensureActive()
            val result = try {
                updatePlugin(activePlugin, release, latestVersion)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                installationFailure(activePlugin.target, exception)
            }
            onResult(result)
            result
        }
    }

    private suspend fun updatePlugin(
        activePlugin: ActivePlugin,
        release: LatestGitHubRelease,
        latestVersion: Version
    ): PluginUpdateResult {
        val target = activePlugin.target
        val plugin = target.plugin
        if (latestVersion <= activePlugin.currentVersion) {
            return PluginUpdateResult.UpToDate(plugin, latestVersion.normalized)
        }

        val asset = when (
            val selection = PluginAssetSelector.select(
                assets = release.assets,
                assetPrefix = target.assetPrefix,
                releaseTag = release.tagName
            )
        ) {
            is AssetSelectionResult.Selected -> selection.asset
            is AssetSelectionResult.Missing -> return PluginUpdateResult.Skipped(
                plugin = plugin,
                reason = SkipReason.NO_MATCHING_ASSET,
                message = "no matching asset; expected ${selection.expectedNames.joinToString()}"
            )

            is AssetSelectionResult.Ambiguous -> return PluginUpdateResult.Failed(
                plugin = plugin,
                repository = target.repository,
                operation = "select asset",
                message = "ambiguous assets: ${selection.candidateNames.joinToString()}"
            )
        }

        val installation = installer.install(plugin, asset, downloadAsset)
        cooldownTracker.markUpdated(plugin.name)
        installation.backupCleanupFailure?.let { exception ->
            diagnostic(
                "Updated ${plugin.name}, but old backup cleanup failed: " +
                        (exception.message ?: exception::class.simpleName.orEmpty())
            )
        }

        return PluginUpdateResult.Updated(
            plugin = plugin,
            previousVersion = plugin.currentVersion,
            installedVersion = latestVersion.normalized,
            installedPath = installation.installedPath,
            backupPath = installation.backupPath
        )
    }

    private fun installationFailure(
        target: PluginUpdateTarget,
        exception: Exception
    ): PluginUpdateResult.Failed {
        val downloadFailure = generateSequence<Throwable>(exception) { it.cause }
            .filterIsInstance<GitHubAssetDownloadException>()
            .firstOrNull()
        val installationFailure = exception as? PluginInstallationException

        return PluginUpdateResult.Failed(
            plugin = target.plugin,
            repository = target.repository,
            operation = installationFailure?.operation ?: "update",
            status = downloadFailure?.status,
            message = exception.message ?: exception::class.simpleName.orEmpty(),
            rateLimit = downloadFailure?.rateLimit,
            assetName = downloadFailure?.assetName,
            cause = exception
        )
    }

    private data class ActivePlugin(
        val target: PluginUpdateTarget,
        val currentVersion: Version
    )

    companion object {
        const val DEFAULT_MAX_CONCURRENCY = 4
    }
}
