package dev.slne.surf.core.paper.listener

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ExecuteCommandServerListener
import org.bukkit.Bukkit

@AutoService(ExecuteCommandServerListener::class)
class PaperExecuteCommandListener : ExecuteCommandServerListener {
    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String
    ): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }
}