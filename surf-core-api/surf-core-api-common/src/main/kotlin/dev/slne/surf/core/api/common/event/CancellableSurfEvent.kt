package dev.slne.surf.core.api.common.event

interface CancellableSurfEvent : SurfEvent {
    var isCancelled: Boolean

    fun cancel() {
        isCancelled = true
    }
}