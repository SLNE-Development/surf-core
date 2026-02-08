package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime

/**
 * Represents a system error that occurred during execution.
 * This is separate from player-specific errors (SurfCoreError).
 */
data class SystemError(
    val id: Long,
    val errorMessage: String,
    val stacktrace: String,
    val location: String,
    val server: String,
    val firstOccurred: OffsetDateTime,
    val lastOccurred: OffsetDateTime,
    val occurrenceCount: Int
)
