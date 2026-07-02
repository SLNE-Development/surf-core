package dev.slne.surf.core.launcher.server.updater

import java.nio.file.Path


/**
 * Represents a plugin that can be updated.
 *
 * @property name The name of the plugin, e.g. surf-core-paper, surf-api-paper-server, surf-captcha or surf-core-velocity.
 * @property currentVersion The current version of the plugin.
 */
data class UpdatablePlugin(
    val name: String,
    val currentVersion: String,
    val jarPath: Path
) {
    /**
     * The Plugin name, e.g. core, captcha or api
     */
    fun findPluginName(): String {
        if (name == "surf-paper-api") { // Special case for the old API plugin name
            return "api"
        }

        return name.substringAfter("surf-").substringBefore("-paper").substringBefore("-velocity")
            .replace("-server", "").replace("-api", "")
    }

    fun findPluginType() = when {
        name.contains("-paper") -> "paper"
        name.contains("-velocity") -> "velocity"
        else -> null
    }

    val latestReleaseUrl get() = "https://api.github.com/repos/SLNE-DEVELOPMENT/surf-${findPluginName()}/releases/latest"
}
