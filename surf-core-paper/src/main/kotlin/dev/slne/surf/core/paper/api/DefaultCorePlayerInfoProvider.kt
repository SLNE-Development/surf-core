package dev.slne.surf.core.paper.api

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.CorePlayerInfoProvider
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

object DefaultCorePlayerInfoProvider : CorePlayerInfoProvider {
    override fun createServerInfo(playerUuid: UUID): CorePlayerInfoProvider.ServerInfo? {
        val player = Bukkit.getPlayer(playerUuid) ?: return null

        return CorePlayerInfoProvider.ServerInfo(
            serverName = SurfServer.current().name,
            serverDisplayName = SurfServer.current().displayName,
            serverRegion = when (player.world.environment) {
                World.Environment.NETHER -> "Nether"
                World.Environment.THE_END -> "End"
                else -> "Overworld"
            }
        )
    }
}