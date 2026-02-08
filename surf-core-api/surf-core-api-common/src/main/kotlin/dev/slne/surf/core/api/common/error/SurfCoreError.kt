package dev.slne.surf.core.api.common.error

data class SurfCoreError(
    val code: String,
    val message: String,
    val server: String
)
