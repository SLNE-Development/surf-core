package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.coroutine.MinestomDispatchers
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.RemoteCommandExecutor
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer

@AutoService(RemoteCommandExecutor::class)
class MinestomRemoteCommandExecutor : RemoteCommandExecutor {
    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String,
    ): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        return withContext(MinestomDispatchers.Main) {
            MinecraftServer.getCommandManager().execute(
                MinecraftServer.getCommandManager().consoleSender,
                command,
            )
            true
        }
    }
}
