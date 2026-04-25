package dev.slne.surf.core.core.common.redis.listener

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import net.kyori.adventure.text.Component

interface ShutdownServerListener {
    fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean

    companion object : ShutdownServerListener by requiredService<ShutdownServerListener>()
}