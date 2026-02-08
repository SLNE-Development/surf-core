package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.core.core.common.error.SystemErrorService
import dev.slne.surf.core.fallback.repository.systemErrorRepository
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services

@AutoService(SystemErrorService::class)
class SystemErrorServiceImpl : SystemErrorService, Services.Fallback {
    override suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SystemError = systemErrorRepository.logError(message, stacktrace, location, server)

    override suspend fun getAllErrors(): ObjectList<SystemError> =
        systemErrorRepository.getAllErrors()

    override suspend fun getError(id: Long): SystemError? =
        systemErrorRepository.getError(id)
}
