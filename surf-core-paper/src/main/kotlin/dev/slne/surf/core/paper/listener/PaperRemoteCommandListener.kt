package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.RemoteCommandExecutor
import dev.slne.surf.core.paper.plugin
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit

@AutoService(RemoteCommandExecutor::class)
class PaperRemoteCommandListener : RemoteCommandExecutor {
    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String
    ): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        return withContext(plugin.globalRegionDispatcher) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }
    }
}