package dev.slne.surf.core.launcher.server.updater.process

import dev.slne.surf.core.launcher.server.updater.GitHubRepositoryCoordinates
import dev.slne.surf.core.launcher.server.updater.UpdatablePlugin
import java.nio.file.Path

sealed interface PluginUpdateResult {
    val plugin: UpdatablePlugin

    data class UpToDate(
        override val plugin: UpdatablePlugin,
        val latestVersion: String
    ) : PluginUpdateResult

    data class Updated(
        override val plugin: UpdatablePlugin,
        val previousVersion: String,
        val installedVersion: String,
        val installedPath: Path,
        val backupPath: Path?
    ) : PluginUpdateResult

    data class Skipped(
        override val plugin: UpdatablePlugin,
        val reason: SkipReason,
        val message: String
    ) : PluginUpdateResult

    data class Failed(
        override val plugin: UpdatablePlugin,
        val repository: GitHubRepositoryCoordinates?,
        val operation: String,
        val message: String,
        val status: Int? = null,
        val rateLimit: String? = null,
        val assetName: String? = null,
        val cause: Throwable? = null
    ) : PluginUpdateResult
}

enum class SkipReason {
    COOLDOWN,
    DUPLICATE_PLUGIN_ID,
    MALFORMED_CURRENT_VERSION,
    MALFORMED_RELEASE_VERSION,
    NO_PUBLISHED_RELEASE,
    NO_MATCHING_ASSET
}

data class PluginUpdateSummary(
    val checkedPluginCount: Int,
    val updatedCount: Int,
    val upToDateCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
    val durationMs: Long
) {
    companion object {
        fun from(
            checkedPluginCount: Int,
            results: Collection<PluginUpdateResult>,
            durationMs: Long
        ) = PluginUpdateSummary(
            checkedPluginCount = checkedPluginCount,
            updatedCount = results.count { it is PluginUpdateResult.Updated },
            upToDateCount = results.count { it is PluginUpdateResult.UpToDate },
            skippedCount = results.count { it is PluginUpdateResult.Skipped },
            failedCount = results.count { it is PluginUpdateResult.Failed },
            durationMs = durationMs.coerceAtLeast(0)
        )
    }
}
