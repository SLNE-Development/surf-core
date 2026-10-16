package dev.slne.surf.core.velocity.join

import dev.slne.surf.core.core.common.join.MaxPlayersJoinListener
import dev.slne.surf.core.velocity.plugin
import java.util.*

object VelocityMaxPlayersJoinListener : MaxPlayersJoinListener() {
    override fun maxPlayers() = plugin.proxy.configuration.showMaxPlayers
    override fun playerCount() = plugin.proxy.playerCount
    override fun isConnected(playerUuid: UUID) = plugin.proxy.getPlayer(playerUuid).isPresent
}
