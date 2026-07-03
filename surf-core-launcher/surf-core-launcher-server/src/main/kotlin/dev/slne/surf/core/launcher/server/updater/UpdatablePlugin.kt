package dev.slne.surf.core.launcher.server.updater

import java.nio.file.Path

private val specialPluginNames = mapOf(
    "surf-paper-api" to "api",
    "surf-skill-paper" to "skills",
    "surf-death-messages" to "deathmessages"
)

val specialAssetNames = mapOf(
    "a" to "b"
)

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
    fun findPluginName(mapped: Boolean = true) =
        specialPluginNames[name]?.takeIf { mapped } ?: name.substringAfter("surf-")
            .substringBefore("-paper")
            .substringBefore("-velocity")
            .replace("-server", "").replace("-api", "")

    fun findPluginType() = when {
        name.contains("-paper") -> "paper"
        name.contains("-velocity") -> "velocity"
        else -> null
    }

    val latestReleaseUrl get() = "https://api.github.com/repos/SLNE-DEVELOPMENT/surf-${findPluginName()}/releases/latest"
}
