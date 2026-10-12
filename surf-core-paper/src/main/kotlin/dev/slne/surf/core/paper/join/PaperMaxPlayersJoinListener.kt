package dev.slne.surf.core.paper.join

import dev.slne.surf.core.core.common.join.MaxPlayersJoinListener
import org.bukkit.Bukkit
import java.util.*

object PaperMaxPlayersJoinListener : MaxPlayersJoinListener() {
    override fun maxPlayers() = Bukkit.getMaxPlayers()
    override fun playerCount() = Bukkit.getOnlinePlayers().size
    override fun isConnected(playerUuid: UUID) = Bukkit.getPlayer(playerUuid) != null
}
