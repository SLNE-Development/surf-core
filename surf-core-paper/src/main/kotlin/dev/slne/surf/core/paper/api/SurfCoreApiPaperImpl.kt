package dev.slne.surf.core.paper.api

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.client.SurfCoreApiClientImpl
import dev.slne.surf.core.paper.surfServerConfig
import net.kyori.adventure.util.Services

@AutoService(SurfCoreApi::class)
class SurfCoreApiPaperImpl : SurfCoreApiClientImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
    override fun getCurrentServerCategory() = surfServerConfig.serverCategory
    override fun getCurrentServerDisplayName() = surfServerConfig.serverDisplayName
}