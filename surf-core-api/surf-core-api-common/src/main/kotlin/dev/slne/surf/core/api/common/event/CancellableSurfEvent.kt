package dev.slne.surf.core.api.common.event

import kotlinx.serialization.Serializable

@Serializable
sealed interface CancellableSurfEvent : SurfEvent {
    var isCancelled: Boolean

    fun cancel() {
        isCancelled = true
    }
}