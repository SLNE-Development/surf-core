package dev.slne.surf.core.api.common.util

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer

inline fun SurfPlayer.sendText(text: SurfComponentBuilder.() -> Unit) {
    SurfCoreApi.sendText(this, SurfComponentBuilder(text))
}