package dev.slne.surf.core.paper.api

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.core.common.SurfCoreApiImpl
import dev.slne.surf.core.paper.surfServerConfig
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import net.kyori.adventure.util.Services

@AutoService(SurfCoreApi::class)
class SurfCoreApiPaperImpl : SurfCoreApiImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
    override fun getCurrentServerCategory() = surfServerConfig.serverCategory
    override fun getCurrentServerDisplayName() = surfServerConfig.serverDisplayName
    override fun sendPlayer(
        player: SurfPlayer,
        server: SurfServer
    ) {
        when (server.type) {
            SurfServerType.PROXY -> {
                player.bukkitPlayer?.transfer(
                    server.externalConnectionAddress?.hostName ?: "",
                    server.externalConnectionAddress?.port ?: 0
                )
            }

            SurfServerType.SERVER -> {
                player.bukkitPlayer?.let {
                    surfBukkitApi.sendPlayerToServer(it, server.name)
                }
            }
        }
    }
}