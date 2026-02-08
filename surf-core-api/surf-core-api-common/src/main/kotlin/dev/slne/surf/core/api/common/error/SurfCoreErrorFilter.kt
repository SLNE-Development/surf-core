package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime
import java.util.*

data class SurfCoreErrorFilter(
    val playerUuid: UUID? = null,
    val code: String? = null,
    val messageLike: String? = null,
    val server: String? = null,
    val timestampAfter: OffsetDateTime? = null,
    val timestampBefore: OffsetDateTime? = null,
    val limit: Int = 50
) {
    companion object {
        fun empty() = SurfCoreErrorFilter()
    }
}
