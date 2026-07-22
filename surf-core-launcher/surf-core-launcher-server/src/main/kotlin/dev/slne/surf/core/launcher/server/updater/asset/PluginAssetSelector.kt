package dev.slne.surf.core.launcher.server.updater.asset

import java.net.URI

data class ReleaseAsset(
    val id: Long,
    val name: String,
    val size: Long?,
    val apiUri: URI
)

sealed interface AssetSelectionResult {
    data class Selected(val asset: ReleaseAsset) : AssetSelectionResult
    data class Missing(val expectedNames: List<String>) : AssetSelectionResult
    data class Ambiguous(val candidateNames: List<String>) : AssetSelectionResult
}

object PluginAssetSelector {
    private val excludedClassifiers = setOf(
        "sources",
        "source",
        "javadoc",
        "tests",
        "test",
        "dev",
        "api"
    )

    fun select(
        assets: List<ReleaseAsset>,
        assetPrefix: String,
        releaseTag: String
    ): AssetSelectionResult {
        val tag = releaseTag.trim()
        val normalizedTag = tag.removeSingleLeadingV().substringBefore('+')
        val versionTokens = listOf(normalizedTag, tag.substringBefore('+'))
            .filter(String::isNotBlank)
            .distinct()

        val allNames = versionTokens.map { "$assetPrefix-$it-all.jar" }
        selectUniqueExact(assets, allNames)?.let { return it }

        val regularNames = versionTokens.map { "$assetPrefix-$it.jar" }
        selectUniqueExact(assets, regularNames)?.let { return it }

        val fallbackCandidates = assets
            .filter { asset -> isConstrainedFallback(asset.name, assetPrefix, versionTokens) }
            .sortedBy(ReleaseAsset::name)

        return when (fallbackCandidates.size) {
            0 -> AssetSelectionResult.Missing(allNames + regularNames)
            1 -> AssetSelectionResult.Selected(fallbackCandidates.single())
            else -> AssetSelectionResult.Ambiguous(fallbackCandidates.map(ReleaseAsset::name))
        }
    }

    private fun selectUniqueExact(
        assets: List<ReleaseAsset>,
        expectedNames: List<String>
    ): AssetSelectionResult? {
        val matches = assets
            .filter { asset -> expectedNames.any { it.equals(asset.name, ignoreCase = true) } }
            .sortedBy(ReleaseAsset::name)

        return when (matches.size) {
            0 -> null
            1 -> AssetSelectionResult.Selected(matches.single())
            else -> AssetSelectionResult.Ambiguous(matches.map(ReleaseAsset::name))
        }
    }

    private fun isConstrainedFallback(
        assetName: String,
        assetPrefix: String,
        versionTokens: List<String>
    ): Boolean {
        if (!assetName.endsWith(".jar", ignoreCase = true)) {
            return false
        }

        val prefix = "$assetPrefix-"
        if (!assetName.startsWith(prefix, ignoreCase = true)) {
            return false
        }

        val remainder = assetName
            .drop(prefix.length)
            .dropLast(4)
        val matchingVersion = versionTokens.firstOrNull { token ->
            remainder.equals(token, ignoreCase = true) ||
                    remainder.startsWith("$token-", ignoreCase = true)
        } ?: return false
        val classifier = remainder
            .drop(matchingVersion.length)
            .removePrefix("-")

        if (classifier.isBlank()) {
            return true
        }

        val classifierParts = classifier.lowercase().split('-', '.')
        return classifierParts.none { it in excludedClassifiers }
    }

    private fun String.removeSingleLeadingV() =
        if (startsWith('v', ignoreCase = true)) substring(1) else this
}
