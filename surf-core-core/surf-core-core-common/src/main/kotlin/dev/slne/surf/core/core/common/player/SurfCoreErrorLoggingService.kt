package dev.slne.surf.core.core.common.player

import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.api.common.error.SurfCoreErrorFilter
import dev.slne.surf.surfapi.core.api.util.random
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*

val surfCoreErrorLoggingService = requiredService<SurfCoreErrorLoggingService>()

interface SurfCoreErrorLoggingService {
    suspend fun logError(
        playerUuid: UUID,
        code: String,
        message: String,
        server: String
    ): SurfCoreError

    suspend fun getErrors(playerUuid: UUID): ObjectList<SurfCoreError>
    suspend fun getErrors(filter: SurfCoreErrorFilter): ObjectList<SurfCoreError>
    suspend fun getError(code: String): SurfCoreError?

    fun generateCode(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..7)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}