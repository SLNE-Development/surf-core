package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime
import java.util.*

data class SurfCoreSystemError(
    val uuid: UUID,
    val errorMessage: String,
    val stacktrace: String,
    val location: String,
    val server: String,
    val firstOccurred: OffsetDateTime,
    val lastOccurred: OffsetDateTime,
    val occurrenceCount: Int
) {
    fun getLocationClassName() = location.substringBeforeLast('.')
}
