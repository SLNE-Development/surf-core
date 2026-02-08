package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime
import java.util.*

data class SurfCoreSystemErrorFilter(
    val uuid: UUID? = null,
    val errorCode: String? = null,
    val messageLike: String? = null,
    val locationLike: String? = null,
    val server: String? = null,
    val firstOccurredAfter: OffsetDateTime? = null,
    val firstOccurredBefore: OffsetDateTime? = null,
    val lastOccurredAfter: OffsetDateTime? = null,
    val lastOccurredBefore: OffsetDateTime? = null,
    val minOccurrenceCount: Int? = null,
    val limit: Int = 50
) {
    companion object {
        fun empty() = SurfCoreSystemErrorFilter()
    }
}
