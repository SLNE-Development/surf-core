package dev.slne.surf.core.api.common.error

import java.time.OffsetDateTime

data class SurfCoreError(
    val code: String,
    val message: String,
    val server: String,
    val timestamp: OffsetDateTime
)
