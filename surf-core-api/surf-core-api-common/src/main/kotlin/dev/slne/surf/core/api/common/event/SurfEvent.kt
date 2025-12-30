package dev.slne.surf.core.api.common.event

import kotlinx.serialization.Serializable

/**
 * Base interface for all Surf events.
 * Every SurfEvent has to be serializable and marked with @Serializable annotation.
 */
@Serializable
sealed interface SurfEvent