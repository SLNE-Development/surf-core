package dev.slne.surf.core.launcher.server.updater.process

import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import dev.slne.surf.core.launcher.server.updater.GITHUB_ORGANIZATION
import dev.slne.surf.core.launcher.server.updater.GitHubRepositoryCoordinates
import dev.slne.surf.core.launcher.server.updater.cooldown.UpdateCooldownTracker
import dev.slne.surf.core.launcher.server.updater.github.GitHubReleaseClient
import dev.slne.surf.core.launcher.server.updater.github.LatestReleaseResult
import dev.slne.surf.core.launcher.server.updater.install.PluginInstaller
import dev.slne.surf.core.launcher.server.updater.version.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.TimeSource

object PluginUpdater {
    private val pluginsPath = Path.of("plugins")
    private val githubToken by lazy {
        resolveGitHubToken(
            environmentToken = System.getenv(GITHUB_TOKEN_ENVIRONMENT_VARIABLE),
            configuredToken = CoreLauncher.config.personalAccessToken
        )
    }
    private val githubClient by lazy { GitHubReleaseClient(githubToken) }
    private val cooldownTracker by lazy {
        UpdateCooldownTracker(
            persistencePath = pluginsPath.resolve(".last-updates"),
            diagnostic = { message -> diagnostic("(Cooldown) $message") }
        )
    }
    private val scanner by lazy {
        PluginScanner(
            pluginsPath = pluginsPath,
            ignoredPluginIds = CoreLauncher.config.autoUpdateIgnoredPlugins.toSet(),
            warning = { message -> scannerLog(message) },
            diagnostic = { message -> diagnostic(message, SCANNER_COMPONENT) }
        )
    }
    private val updateService by lazy {
        PluginUpdateService(
            cooldownTracker = cooldownTracker,
            installer = PluginInstaller(pluginsPath),
            fetchLatestRelease = githubClient::fetchLatestRelease,
            downloadAsset = githubClient::downloadAsset,
            diagnostic = { message -> diagnostic(message) }
        )
    }

    val hasConfiguredToken: Boolean
        get() = githubToken != null

    suspend fun start() {
        val started = TimeSource.Monotonic.markNow()
        val completedResults = ConcurrentLinkedQueue<PluginUpdateResult>()
        var checkedPluginCount = 0

        try {
            val plugins = scanner.findPlugins()
            checkedPluginCount = plugins.size
            if (plugins.isEmpty()) {
                updaterLog("No plugins found for update checking")
                return
            }

            updaterLog("Checking updates for ${plugins.size} plugins")
            updateService.update(plugins) { result ->
                completedResults += result
                logResult(result)
            }
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    cooldownTracker.persist()
                } catch (exception: Exception) {
                    updaterLog(
                        "Could not persist update cooldowns: " +
                                (exception.message ?: exception::class.simpleName.orEmpty())
                    )
                }
            }

            val summary = PluginUpdateSummary.from(
                checkedPluginCount = checkedPluginCount,
                results = completedResults,
                durationMs = started.elapsedNow().inWholeMilliseconds
            )
            updaterLog(
                "Summary: checked=${summary.checkedPluginCount}, updated=${summary.updatedCount}, " +
                        "up-to-date=${summary.upToDateCount}, skipped=${summary.skippedCount}, " +
                        "failed=${summary.failedCount}, duration=${summary.durationMs}ms"
            )
        }
    }

    suspend fun checkForCoreLauncherUpdate() {
        val currentVersionText = LauncherConstants::class.java.`package`?.implementationVersion
            ?: return
        val currentVersion = Version.parse(currentVersionText)
        if (currentVersion == null) {
            diagnostic("Installed launcher version '$currentVersionText' is malformed", GITHUB_COMPONENT)
            return
        }

        when (val result = githubClient.fetchLatestRelease(CORE_REPOSITORY)) {
            is LatestReleaseResult.Found -> {
                val latestVersion = Version.parse(result.value.tagName)
                if (latestVersion == null) {
                    diagnostic(
                        "Core launcher release tag '${result.value.tagName}' is malformed",
                        GITHUB_COMPONENT
                    )
                    return
                }
                if (latestVersion <= currentVersion) {
                    return
                }

                val releaseUrl = result.value.release.htmlUrl.toExternalForm()
                githubLog("*".repeat(80))
                githubLog(
                    "A new CoreLauncher version is available " +
                            "(${currentVersion.normalized} -> ${latestVersion.normalized})"
                )
                githubLog("Release: $releaseUrl")
                githubLog("*".repeat(80))
            }

            is LatestReleaseResult.RepositoryUnavailable -> diagnostic(
                "Core repository was not found or the token lacks access",
                GITHUB_COMPONENT
            )

            is LatestReleaseResult.NoPublishedRelease -> diagnostic(
                "Core repository has no published release",
                GITHUB_COMPONENT
            )

            is LatestReleaseResult.Failed -> diagnostic(
                buildString {
                    append("Core update check failed: repository=${result.repository}, operation=${result.operation}")
                    result.status?.let { append(", http=$it") }
                    append(", message=${result.message}")
                    result.rateLimit?.let { append(", rate-limit=$it") }
                },
                GITHUB_COMPONENT
            )
        }
    }

    private fun logResult(result: PluginUpdateResult) {
        when (result) {
            is PluginUpdateResult.UpToDate -> updaterLog(
                "${result.plugin.name} is current at ${result.latestVersion}"
            )

            is PluginUpdateResult.Updated -> updaterLog(
                "Updated ${result.plugin.name} from ${result.previousVersion} " +
                        "to ${result.installedVersion}"
            )

            is PluginUpdateResult.Skipped -> updaterLog(
                "Skipped ${result.plugin.name}: ${result.message}"
            )

            is PluginUpdateResult.Failed -> {
                updaterLog(
                    buildString {
                        append("Failed ${result.plugin.name}: operation=${result.operation}")
                        result.repository?.let { append(", repository=$it") }
                        result.status?.let { append(", http=$it") }
                        result.assetName?.let { append(", asset=$it") }
                        append(", message=${result.message}")
                        result.rateLimit?.let { append(", rate-limit=$it") }
                    }
                )
                if (CoreLauncher.config.logGithubReleaseFetchFailures) {
                    result.cause?.printStackTrace()
                }
            }
        }
    }

    private fun updaterLog(message: String) = println("$LOG_PREFIX $UPDATER_COMPONENT $message")

    private fun githubLog(message: String) = println("$LOG_PREFIX $GITHUB_COMPONENT $message")

    private fun scannerLog(message: String) = println("$LOG_PREFIX $SCANNER_COMPONENT $message")

    private fun diagnostic(message: String, component: String = UPDATER_COMPONENT) {
        if (CoreLauncher.config.logGithubReleaseFetchFailures) {
            println("$LOG_PREFIX $component $message")
        }
    }

    private val CORE_REPOSITORY = GitHubRepositoryCoordinates(GITHUB_ORGANIZATION, "surf-core")
    private const val GITHUB_TOKEN_ENVIRONMENT_VARIABLE = "SURF_GITHUB_TOKEN"
    private const val UPDATER_COMPONENT = "(Updater)"
    private const val GITHUB_COMPONENT = "(GitHub)"
    private const val SCANNER_COMPONENT = "(Scanner)"
}

internal fun resolveGitHubToken(environmentToken: String?, configuredToken: String?): String? =
    environmentToken?.trim()?.takeIf(String::isNotEmpty)
        ?: configuredToken?.trim()?.takeIf(String::isNotEmpty)
