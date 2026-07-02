package dev.slne.surf.core.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.core.common.SurfCoreApiImpl

@AutoService(SurfCoreApi::class)
class SurfCoreApiMicroserviceImpl : SurfCoreApiImpl() {
    override fun getCurrentServerName() = "surf-core"
    override fun getCurrentServerDisplayName() = "surf-core"
    override fun getCurrentServerCategory() = "microservice"

    override suspend fun loadOfflinePlayerNameEntries() = error("Not available on microservice")
}