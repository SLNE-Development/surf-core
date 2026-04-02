package dev.slne.surf.core.paper.api.access

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.CorePlayerStatusAccess
import dev.slne.surf.core.api.paper.PlayerAccessHandler
import net.kyori.adventure.util.Services
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

@AutoService(CorePlayerStatusAccess::class)
class CorePlayerStatusAccessImpl : CorePlayerStatusAccess, Services.Fallback {
    private val _handlers = ConcurrentHashMap.newKeySet<PlayerAccessHandler>()

    override fun registerHandler(handler: PlayerAccessHandler) {
        _handlers += handler
    }

    override fun hasAccess(
        viewer: Player,
        player: Player
    ): Boolean {
        val surfViewer = SurfCoreApi.getPlayer(viewer.uniqueId) ?: return false
        val surfPlayer = SurfCoreApi.getPlayer(player.uniqueId) ?: return false

        return hasAccess(surfViewer, surfPlayer)
    }

    override fun hasAccess(
        viewer: CommandSender,
        player: SurfPlayer
    ): Boolean {
        if (viewer !is Player) return true
        val surfViewer = SurfCoreApi.getPlayer(viewer.uniqueId) ?: return false

        return hasAccess(surfViewer, player)
    }

    override fun hasAccess(
        viewer: SurfPlayer,
        player: SurfPlayer
    ): Boolean = _handlers.all { it(viewer, player) }
}