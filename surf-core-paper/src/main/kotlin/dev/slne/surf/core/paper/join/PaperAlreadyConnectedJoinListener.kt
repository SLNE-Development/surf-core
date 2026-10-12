package dev.slne.surf.core.paper.join

import dev.slne.surf.core.core.common.join.AlreadyConnectedJoinListener
import org.bukkit.Bukkit
import java.util.*

object PaperAlreadyConnectedJoinListener : AlreadyConnectedJoinListener() {
    override fun isConnected(playerUuid: UUID) = Bukkit.getPlayer(playerUuid) != null
}
