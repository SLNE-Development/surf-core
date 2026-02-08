package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.core.core.common.error.SurfCoreSystemErrorService
import dev.slne.surf.core.fallback.repository.surfCoreSystemErrorRepository
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services

@AutoService(SurfCoreSystemErrorService::class)
class SurfCoreSystemErrorServiceImpl : SurfCoreSystemErrorService, Services.Fallback {
    override suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SurfCoreSystemError =
        surfCoreSystemErrorRepository.logError(message, stacktrace, location, server)

    override suspend fun getAllErrors(): ObjectList<SurfCoreSystemError> =
        surfCoreSystemErrorRepository.getAllErrors()

    override suspend fun getError(id: Long): SurfCoreSystemError? =
        surfCoreSystemErrorRepository.getError(id)
}
