package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ServerShutdownHandler
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import kotlin.concurrent.thread

@AutoService(ServerShutdownHandler::class)
class MinestomShutdownHandler : ServerShutdownHandler {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        MinecraftServer.getConnectionManager().onlinePlayers.toList().forEach { player ->
            player.kick(reason ?: Component.text("The server is shutting down."))
        }

        thread(isDaemon = false, name = "surf-core-minestom-shutdown") {
            MinecraftServer.stopCleanly()
        }
        return true
    }
}
