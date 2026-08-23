package dev.slne.surf.core.client.player.error

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.rabbit.packet.player.error.SaveSurfPlayerErrorRequestPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*
import kotlin.random.Random

@AutoService(SurfPlayerErrorService::class)
class SurfPlayerErrorServiceImpl : SurfPlayerErrorService, Services.Fallback {
    override suspend fun saveErrorAsync(
        playerUuid: UUID,
        occurredOn: String,
        occurredAt: OffsetDateTime,
        staffMessage: String,
        errorCode: String
    ) = ClientCoreInstance.rabbitApi.sendRequest(
        SaveSurfPlayerErrorRequestPacket(
            playerUuid = playerUuid,
            occurredOn = occurredOn,
            occurredAt = occurredAt,
            staffMessage = staffMessage,
            errorCode = errorCode
        )
    ).error

    override fun saveError(
        playerUuid: UUID,
        staffMessage: String,
        scope: CoroutineScope
    ): String {
        val errorCode = generateErrorCode()
        val occurredOn = CommonSurfServer.current().name
        val occurredAt = OffsetDateTime.now()

        scope.launch {
            saveErrorAsync(
                playerUuid = playerUuid,
                occurredOn = occurredOn,
                occurredAt = occurredAt,
                staffMessage = staffMessage,
                errorCode = errorCode
            )
        }

        return errorCode
    }


    fun generateErrorCode(): String = String(
        CharArray(ERROR_CODE_LENGTH) {
            ERROR_CODE_ALPHABET[Random.nextInt(ERROR_CODE_ALPHABET.size)]
        }
    )

    companion object {
        private val ERROR_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
        private const val ERROR_CODE_LENGTH = 8
    }
}