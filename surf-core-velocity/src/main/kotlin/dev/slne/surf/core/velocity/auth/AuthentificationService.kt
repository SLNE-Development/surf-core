package dev.slne.surf.core.velocity.auth

import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.surfapi.core.api.messages.adventure.key
import java.util.*
import kotlin.time.Duration.Companion.seconds

val authentificationService = AuthentificationService()

class AuthentificationService {
    val authMap = redisApi.createSyncMap<UUID, ByteArray>("surf-core:authentification", 15.seconds)
    val key = key("surf-core", "transfer-authentification")

    fun authenticate(uuid: UUID, token: ByteArray): Boolean {
        val storedToken = authMap.remove(uuid) ?: return false
        return storedToken.contentEquals(token)
    }

    fun preTransfer(uuid: UUID, token: ByteArray) {
        authMap[uuid] = token
    }

    fun init() = Unit
}