package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.core.common.player.SurfCoreErrorLoggingService
import dev.slne.surf.core.fallback.repository.surfCoreErrorLoggingRepository
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfCoreErrorLoggingService::class)
class SurfCoreErrorLoggingServiceImpl : SurfCoreErrorLoggingService, Services.Fallback {
    override suspend fun logError(
        playerUuid: UUID,
        code: String,
        message: String,
        server: String,
        stacktrace: String,
        location: String
    ): SurfCoreError = surfCoreErrorLoggingRepository.logError(
        playerUuid,
        code,
        message,
        server,
        stacktrace,
        location
    )

    override suspend fun getErrors(playerUuid: UUID): ObjectList<SurfCoreError> =
        surfCoreErrorLoggingRepository.getErrors(playerUuid)

    override suspend fun getError(code: String): SurfCoreError? =
        surfCoreErrorLoggingRepository.getError(code)
}