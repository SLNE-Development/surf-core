package dev.slne.surf.core.api.paper

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.player.SurfPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val access = requiredService<CorePlayerStatusAccess>()
typealias PlayerAccessHandler = (viewer: SurfPlayer, player: SurfPlayer) -> Boolean

interface CorePlayerStatusAccess {
    fun registerHandler(handler: PlayerAccessHandler)
    fun hasAccess(viewer: Player, player: Player): Boolean
    fun hasAccess(viewer: CommandSender, player: SurfPlayer): Boolean
    fun hasAccess(viewer: SurfPlayer, player: SurfPlayer): Boolean

    companion object : CorePlayerStatusAccess by access {
        val INSTANCE get() = access
    }
}