package dev.slne.surf.core.launcher.server.updater

import java.nio.file.Path

internal const val GITHUB_ORGANIZATION = "SLNE-Development"

data class UpdatablePlugin(
    val name: String,
    val currentVersion: String,
    val jarPath: Path
)

data class GitHubRepositoryCoordinates(
    val owner: String,
    val name: String
) {
    val fullName = "$owner/$name"

    override fun toString() = fullName
}

data class PluginUpdateTarget(
    val plugin: UpdatablePlugin,
    val repository: GitHubRepositoryCoordinates,
    val assetPrefix: String
)

/**
 * The single source of truth for repository and release-asset naming exceptions.
 */
object PluginRepositoryMappings {
    private val repositoryNameOverrides = mapOf(
        "surf-paper-api" to "api",
        "surf-skill-paper" to "skills",
        "surf-death-messages" to "deathmessages"
    )

    private val assetPrefixOverrides = mapOf(
        "surf-paper-paper" to "surf-api-paper"
    )

    fun resolve(plugin: UpdatablePlugin): PluginUpdateTarget {
        val derivedName = derivePluginName(plugin.name)
        val repositoryName = repositoryNameOverrides[plugin.name] ?: derivedName
        val pluginType = when {
            plugin.name.contains("-paper") -> "paper"
            plugin.name.contains("-velocity") -> "velocity"
            else -> null
        }
        val derivedAssetPrefix = buildString {
            append("surf-")
            append(derivedName)
            pluginType?.let {
                append('-')
                append(it)
            }
        }

        return PluginUpdateTarget(
            plugin = plugin,
            repository = GitHubRepositoryCoordinates(
                owner = GITHUB_ORGANIZATION,
                name = "surf-$repositoryName"
            ),
            assetPrefix = assetPrefixOverrides[derivedAssetPrefix] ?: derivedAssetPrefix
        )
    }

    private fun derivePluginName(pluginId: String) = pluginId
        .substringAfter("surf-")
        .substringBefore("-paper")
        .substringBefore("-velocity")
        .replace("-server", "")
        .replace("-api", "")
}
