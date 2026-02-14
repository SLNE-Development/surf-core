package dev.slne.surf.core.velocity.api

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.SurfCoreApiImpl
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.surfServerConfig
import net.kyori.adventure.util.Services
import kotlin.jvm.optionals.getOrNull

@AutoService(SurfCoreApi::class)
class SurfCoreApiVelocityImpl : SurfCoreApiImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
    override fun getCurrentServerDisplayName() = surfServerConfig.serverDisplayName
    override fun getCurrentServerCategory() = surfServerConfig.serverCategory
    
    override fun sendPlayer(
        player: SurfPlayer,
        server: CommonSurfServer
    ) {
        when (server) {
            is SurfProxyServer -> {
                plugin.proxy.getPlayer(player.uuid).getOrNull()?.transferToHost(
                    server.address
                )
            }

            is SurfServer -> {
                val velocityServer = plugin.proxy.getServer(server.name).getOrNull()
                    ?: error("SurfServer ${server.name} not found on proxy")
                plugin.proxy.getPlayer(player.uuid).getOrNull()
                    ?.createConnectionRequest(velocityServer)?.fireAndForget()
            }
        }
    }
}