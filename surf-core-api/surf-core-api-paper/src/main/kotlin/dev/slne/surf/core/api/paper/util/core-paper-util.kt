package dev.slne.surf.core.api.paper.util

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent

fun Player.toSurfPlayer() = SurfCoreApi.getPlayer(this.uniqueId)
    ?: error("SurfPlayer for Bukkit Player ${this.name} not found!")

fun SurfPlayer.toPlayer() = Bukkit.getPlayer(this.uuid)

val Player.surfPlayer get() = this.toSurfPlayer()
val SurfPlayer.bukkitPlayer get() = this.toPlayer()

val PlayerEvent.surfPlayer get() = this.player.surfPlayer