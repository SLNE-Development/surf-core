package dev.slne.surf.core.velocity.listener

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.redis.listener.RemoteCommandExecutor
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.proxy
import kotlinx.coroutines.future.await

@AutoService(RemoteCommandExecutor::class)
class VelocityRemoteCommandListener : RemoteCommandExecutor {
    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String
    ): Boolean? {
        if (commonSurfServer.uuid != SurfProxyServer.current().uuid) {
            return null
        }

        return plugin.proxy.commandManager.executeAsync(proxy.consoleCommandSource, command).await()
    }
}