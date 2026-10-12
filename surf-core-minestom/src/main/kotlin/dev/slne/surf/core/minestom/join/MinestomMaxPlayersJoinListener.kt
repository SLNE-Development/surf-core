package dev.slne.surf.core.minestom.join

import dev.slne.surf.api.minestom.player.PlayerLimit
import dev.slne.surf.core.core.common.join.MaxPlayersJoinListener
import net.minestom.server.MinecraftServer
import java.util.*

object MinestomMaxPlayersJoinListener : MaxPlayersJoinListener() {
    override fun maxPlayers() = PlayerLimit.maxPlayers
    override fun playerCount() = PlayerLimit.playerCount
    override fun isConnected(playerUuid: UUID) =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUuid) != null
}
