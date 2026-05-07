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

        val shutdownThread = Thread {
            try {
                Thread.sleep(100)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            plugin.proxy.shutdown(reason)
        }
        shutdownThread.isDaemon = false
        shutdownThread.start()
        return true
    }
}