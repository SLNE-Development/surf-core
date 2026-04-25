package dev.slne.surf.core.velocity.listener

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.redis.listener.ShutdownServerListener
import dev.slne.surf.core.velocity.plugin
import net.kyori.adventure.text.Component

@AutoService(ShutdownServerListener::class)
class VelocityShutdownListener : ShutdownServerListener {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean? {
        if (commonSurfServer.uuid != SurfProxyServer.current().uuid) {
            return null
        }

        plugin.proxy.shutdown(reason)
        return true
    }
}