package dev.slne.surf.core.api.common.util

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfEvent

inline fun <reified T : SurfEvent> SurfCoreApi.subscribe(noinline handler: (T) -> Unit) {
    subscribe(T::class) { event ->
        handler(event as T)
    }
}
