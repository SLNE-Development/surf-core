package dev.slne.surf.core.core.common.player.error

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.core.api.common.player.error.SurfPlayerError
import kotlinx.coroutines.CoroutineScope
import java.time.OffsetDateTime
import java.util.*

private val service = requiredService<SurfPlayerErrorService>()

interface SurfPlayerErrorService {
    suspend fun saveErrorAsync(
        playerUuid: UUID,
        occurredOn: String,
        occurredAt: OffsetDateTime,
        staffMessage: String,
        errorCode: String
    ): SurfPlayerError

    fun saveError(
        playerUuid: UUID,
        staffMessage: String,
        scope: CoroutineScope
    ): String

    companion object : SurfPlayerErrorService by service {
        val INSTANCE get() = service
    }
}