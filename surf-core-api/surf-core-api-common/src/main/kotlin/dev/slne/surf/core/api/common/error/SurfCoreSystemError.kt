package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime

data class SurfCoreSystemError(
    val id: Long,
    val errorMessage: String,
    val stacktrace: String,
    val location: String,
    val server: String,
    val firstOccurred: OffsetDateTime,
    val lastOccurred: OffsetDateTime,
    val occurrenceCount: Int
)
