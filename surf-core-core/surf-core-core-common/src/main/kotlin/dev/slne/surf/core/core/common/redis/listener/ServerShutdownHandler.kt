package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import net.kyori.adventure.text.Component

private val listener = requiredService<ServerShutdownHandler>()

interface ServerShutdownHandler {
    fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean?

    companion object : ServerShutdownHandler by listener
}