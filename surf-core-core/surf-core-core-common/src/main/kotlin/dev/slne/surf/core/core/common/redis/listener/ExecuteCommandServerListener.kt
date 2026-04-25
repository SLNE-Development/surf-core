package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.server.CommonSurfServer

private val listener = requiredService<ExecuteCommandServerListener>()

interface ExecuteCommandServerListener {
    suspend fun executeCommand(commonSurfServer: CommonSurfServer, command: String): Boolean?

    companion object :
        ExecuteCommandServerListener by listener
}