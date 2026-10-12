package dev.slne.surf.core.minestom.join

import dev.slne.surf.core.core.common.join.AlreadyConnectedJoinListener
import net.minestom.server.MinecraftServer
import java.util.*

object MinestomAlreadyConnectedJoinListener : AlreadyConnectedJoinListener() {
    override fun isConnected(playerUuid: UUID) =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUuid) != null
}
