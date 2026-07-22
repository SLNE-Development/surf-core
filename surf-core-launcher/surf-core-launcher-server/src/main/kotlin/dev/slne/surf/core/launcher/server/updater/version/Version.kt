package dev.slne.surf.core.launcher.server.updater.version

import java.math.BigInteger

/**
 * A small immutable semantic-version value used by both plugin and launcher checks.
 * Numeric core components may have any length; missing trailing components compare as zero.
 */
class Version private constructor(
    private val core: List<BigInteger>,
    private val prerelease: List<PrereleaseIdentifier>,
    val normalized: String
) : Comparable<Version> {
    val isPrerelease: Boolean
        get() = prerelease.isNotEmpty()

    override fun compareTo(other: Version): Int {
        val coreLength = maxOf(core.size, other.core.size)
        repeat(coreLength) { index ->
            val comparison = core.getOrElse(index) { BigInteger.ZERO }
                .compareTo(other.core.getOrElse(index) { BigInteger.ZERO })
            if (comparison != 0) {
                return comparison
            }
        }

        if (prerelease.isEmpty() || other.prerelease.isEmpty()) {
            return when {
                prerelease.isEmpty() && other.prerelease.isEmpty() -> 0
                prerelease.isEmpty() -> 1
                else -> -1
            }
        }

        val prereleaseLength = minOf(prerelease.size, other.prerelease.size)
        repeat(prereleaseLength) { index ->
            val comparison = prerelease[index].compareTo(other.prerelease[index], index == 0)
            if (comparison != 0) {
                return comparison
            }
        }

        return prerelease.size.compareTo(other.prerelease.size)
    }

    override fun toString() = normalized

    companion object {
        private val versionPattern = Regex(
            """^[vV]?([0-9]+(?:\.[0-9]+)*)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$"""
        )

        fun parse(value: String): Version? {
            val match = versionPattern.matchEntire(value.trim()) ?: return null
            val coreText = match.groupValues[1]
            val prereleaseText = match.groupValues[2]
            val core = coreText.split('.').map { it.toBigInteger() }
            val prerelease = prereleaseText
                .takeIf(String::isNotEmpty)
                ?.split('.')
                ?.map { identifier ->
                    identifier.toBigIntegerOrNull()
                        ?.let(PrereleaseIdentifier::Numeric)
                        ?: PrereleaseIdentifier.Text(identifier.lowercase())
                }
                .orEmpty()
            val normalized = buildString {
                append(coreText)
                if (prereleaseText.isNotEmpty()) {
                    append('-')
                    append(prereleaseText)
                }
            }

            return Version(core, prerelease, normalized)
        }
    }
}

private sealed interface PrereleaseIdentifier {
    fun compareTo(other: PrereleaseIdentifier, stageIdentifier: Boolean): Int

    data class Numeric(val value: BigInteger) : PrereleaseIdentifier {
        override fun compareTo(other: PrereleaseIdentifier, stageIdentifier: Boolean) = when (other) {
            is Numeric -> value.compareTo(other.value)
            is Text -> -1
        }
    }

    data class Text(val value: String) : PrereleaseIdentifier {
        override fun compareTo(other: PrereleaseIdentifier, stageIdentifier: Boolean) = when (other) {
            is Numeric -> 1
            is Text -> if (stageIdentifier) {
                val rankComparison = stageRank(value).compareTo(stageRank(other.value))
                rankComparison.takeIf { it != 0 } ?: value.compareTo(other.value)
            } else {
                value.compareTo(other.value)
            }
        }

        private fun stageRank(value: String) = when (value) {
            "snapshot" -> 0
            "alpha" -> 1
            "beta" -> 2
            "rc" -> 3
            else -> -1
        }
    }
}
