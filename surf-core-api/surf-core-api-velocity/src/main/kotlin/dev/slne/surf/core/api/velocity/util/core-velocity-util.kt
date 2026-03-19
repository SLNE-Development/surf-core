package dev.slne.surf.core.api.velocity.util

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.core.api.common.SurfCoreApi

fun Player.toSurfPlayer() = SurfCoreApi.getPlayer(this.uniqueId)
    ?: error("SurfPlayer for Velocity Player ${this.username} not found!")

val Player.surfPlayer get() = this.toSurfPlayer()