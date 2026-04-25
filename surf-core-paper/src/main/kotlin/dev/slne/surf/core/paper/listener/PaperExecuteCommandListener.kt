package dev.slne.surf.core.paper.listener

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ExecuteCommandServerListener
import org.bukkit.Bukkit

@AutoService(ExecuteCommandServerListener::class)
object PaperExecuteCommandListener : ExecuteCommandServerListener {
    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String
    ): Boolean {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return false
        }

        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }
}