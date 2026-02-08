package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.core.api.common.error.SurfCoreSystemErrorFilter
import dev.slne.surf.core.core.common.error.SurfCoreSystemErrorService
import dev.slne.surf.core.fallback.repository.surfCoreSystemErrorRepository
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services
import java.util.*

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

    override suspend fun getAllErrors(filter: SurfCoreSystemErrorFilter): ObjectList<SurfCoreSystemError> =
        surfCoreSystemErrorRepository.getAllErrors(filter)

    override suspend fun getError(uuid: UUID): SurfCoreSystemError? =
        surfCoreSystemErrorRepository.getError(uuid)
        
    override suspend fun getError(code: String): SurfCoreSystemError? =
        surfCoreSystemErrorRepository.getError(code)
}
