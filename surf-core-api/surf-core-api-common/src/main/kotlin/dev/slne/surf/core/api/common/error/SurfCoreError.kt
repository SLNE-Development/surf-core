package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime
import java.util.*

data class SurfCoreError(
    val playerUuid: UUID,
    val code: String,
    val message: String,
    val server: String,
    val timestamp: OffsetDateTime
)
