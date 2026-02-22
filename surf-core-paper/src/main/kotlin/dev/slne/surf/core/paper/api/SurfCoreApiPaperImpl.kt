package dev.slne.surf.core.paper.api

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.util.bukkitPlayer
import dev.slne.surf.core.core.common.SurfCoreApiImpl
import dev.slne.surf.core.core.common.player.surfCoreErrorLoggingService
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.core.paper.surfServerConfig
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfCoreApi::class)
class SurfCoreApiPaperImpl : SurfCoreApiImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
    override fun getCurrentServerCategory() = surfServerConfig.serverCategory
    override fun logError(playerUuid: UUID, code: String, message: String) {
        plugin.launch {
            surfCoreErrorLoggingService.logError(
                playerUuid,
                code,
                message,
                SurfServer.current().name
            )
        }
    }

    override fun logError(playerUUid: UUID, message: String): String {
        val code = surfCoreErrorLoggingService.generateCode()

        plugin.launch {
            surfCoreErrorLoggingService.logError(
                playerUUid,
                code,
                message,
                SurfServer.current().name
            )
        }

        return code
    }

    override fun getCurrentServerDisplayName() = surfServerConfig.serverDisplayName

    override fun sendPlayer(
        player: SurfPlayer,
        server: CommonSurfServer
    ) {
        when (server) {
            is SurfProxyServer -> {
                player.bukkitPlayer?.transfer(
                    server.address.hostName ?: "",
                    server.address.port
                )
            }

            is SurfServer -> {
                player.bukkitPlayer?.let {
                    surfBukkitApi.sendPlayerToServer(it, server.name)
                }
            }
        }
    }
}