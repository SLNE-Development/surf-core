package dev.slne.surf.core.api.common.event.cancellable

import dev.slne.surf.core.api.common.event.SurfEvent

interface CancellableSurfEvent : SurfEvent {
    var isCancelled: Boolean

    fun cancel() {
        isCancelled = true
    }
}