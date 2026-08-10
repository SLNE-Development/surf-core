package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.extension.CommandManager
import dev.slne.minestom.lobby.api.extension.ConnectionManager
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ServerShutdownHandler
import dev.slne.surf.core.core.common.server.ServerShutdownMessage
import net.kyori.adventure.text.Component

@AutoService(ServerShutdownHandler::class)
class MinestomShutdownHandler : ServerShutdownHandler {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        val message = ServerShutdownMessage.create(reason)
        ConnectionManager.onlinePlayers.forEach { player ->
            player.kick(message)
        }

        CommandManager.executeServerCommand("stop")
        return true
    }
}
