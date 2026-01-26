package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.surfapi.core.api.messages.adventure.key
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import java.util.*
import kotlin.time.Duration.Companion.seconds

val authentificationService = AuthentificationService()

class AuthentificationService {
    val authMap = redisApi.createSyncMap<UUID, ByteArray>("surf-core:authentification", 5.seconds)
    val lastServerMap = redisApi.createSyncMap<UUID, String>("surf-core:last-server", 5.seconds)
    val continuations = mutableObject2ObjectMapOf<UUID, Continuation>()
    val key = key("surf-core", "transfer-authentification")

    fun authenticate(uuid: UUID, token: ByteArray): Boolean {
        val storedToken = authMap.remove(uuid)

        if (storedToken == null) {
            println("[connection] Failed to authenticate player $uuid: no stored token")
            return false
        }

        if (!storedToken.contentEquals(token)) {
            println("[connection] Failed to authenticate player $uuid: invalid token")
            return false
        }

        continuations[uuid]?.resume()
        continuations.remove(uuid)

        return true
    }

    fun preTransfer(uuid: UUID, token: ByteArray) {
        authMap[uuid] = token
    }

    fun init() = Unit
}