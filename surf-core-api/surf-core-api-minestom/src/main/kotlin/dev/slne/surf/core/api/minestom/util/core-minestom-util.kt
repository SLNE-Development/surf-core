package dev.slne.surf.core.api.minestom.util

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

fun Player.toSurfPlayer() = SurfCoreApi.getPlayer(uuid)
    ?: error("SurfPlayer for Minestom Player $username not found!")

val Player.surfPlayer get() = toSurfPlayer()

fun SurfPlayer.toPlayer(): Player? =
    MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid)

val SurfPlayer.minestomPlayer get() = toPlayer()
