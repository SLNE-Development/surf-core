package dev.slne.surf.core.velocity.listener

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ShutdownServerListener
import dev.slne.surf.core.velocity.plugin
import net.kyori.adventure.text.Component

@AutoService(ShutdownServerListener::class)
object VelocityShutdownListener : ShutdownServerListener {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return false
        }

        plugin.proxy.shutdown(reason)
        return true
    }
}