package dev.slne.surf.core.api.common.util

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder

inline fun SurfPlayer.sendText(text: SurfComponentBuilder.() -> Unit) {
    surfCoreApi.sendText(this, SurfComponentBuilder().apply(text).build())
}