package dev.slne.surf.core.velocity.api

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.core.common.SurfCoreApiImpl
import dev.slne.surf.core.velocity.surfServerConfig
import net.kyori.adventure.util.Services

@AutoService(SurfCoreApi::class)
class SurfCoreApiVelocityImpl : SurfCoreApiImpl(), Services.Fallback {
    override fun getCurrentServerName() = surfServerConfig.serverName
}