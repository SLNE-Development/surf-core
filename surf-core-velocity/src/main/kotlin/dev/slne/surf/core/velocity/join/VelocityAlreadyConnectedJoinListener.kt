package dev.slne.surf.core.velocity.join

import dev.slne.surf.core.core.common.join.AlreadyConnectedJoinListener
import dev.slne.surf.core.velocity.plugin
import java.util.*

object VelocityAlreadyConnectedJoinListener : AlreadyConnectedJoinListener() {
    override fun isConnected(playerUuid: UUID) = plugin.proxy.getPlayer(playerUuid).isPresent
}
