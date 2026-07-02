package dev.slne.surf.core.velocity.api

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.client.SurfCoreApiClientImpl
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.redis.handler.convertResult
import dev.slne.surf.core.velocity.surfServerConfig
import kotlinx.coroutines.future.await
import net.kyori.adventure.util.Services
import kotlin.jvm.optionals.getOrNull

@AutoService(SurfCoreApi::class)
class SurfCoreApiVelocityImpl : SurfCoreApiClientImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
    override fun getCurrentServerCategory() = surfServerConfig.serverCategory
    override fun getCurrentServerDisplayName() = surfServerConfig.serverDisplayName

    override suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer
    ): SurfServerConnectResult {
        val player = plugin.proxy.getPlayer(surfPlayer.uuid).getOrNull()
        if (player == null) {
            return super.sendPlayerAwaiting(surfPlayer, surfServer)
        } else {
            val velocityServer = plugin.proxy.getServer(surfServer.name).getOrNull()
                ?: return SurfServerConnectResult(
                    SurfServerConnectResult.Status.SERVER_NOT_FOUND,
                    null
                )

            return player.createConnectionRequest(velocityServer)
                .connect()
                .await()
                .convertResult()
        }
    }
}