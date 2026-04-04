package dev.slne.surf.core.api.paper.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.surfapi.bukkit.api.extensions.pluginManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent
import org.bukkit.permissions.Permission
import org.bukkit.plugin.PluginManager

fun Player.toSurfPlayer() = SurfCoreApi.getPlayer(this.uniqueId)
    ?: error("SurfPlayer for Bukkit Player ${this.name} not found!")

fun SurfPlayer.toPlayer() = Bukkit.getPlayer(this.uuid)

val Player.surfPlayer get() = this.toSurfPlayer()
val SurfPlayer.bukkitPlayer get() = this.toPlayer()

val PlayerEvent.surfPlayer get() = this.player.surfPlayer

fun PluginManager.getOrCreatePermission(name: String): Permission {
    return this.getPermission(name) ?: Permission(name).also { this.addPermission(it) }
}

private val permissionCache: LoadingCache<CommonSurfServer, Permission> =
    Caffeine.newBuilder().build { server ->
        pluginManager.getOrCreatePermission("surf.core.server.${server.name}")
    }

val CommonSurfServer.permission: String get() = permissionCache.get(this).name