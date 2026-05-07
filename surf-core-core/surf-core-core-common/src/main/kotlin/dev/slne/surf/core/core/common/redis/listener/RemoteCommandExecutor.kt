package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.server.CommonSurfServer

private val listener = requiredService<RemoteCommandExecutor>()

interface RemoteCommandExecutor {
    suspend fun executeCommand(commonSurfServer: CommonSurfServer, command: String): Boolean?

    companion object :
        RemoteCommandExecutor by listener
}