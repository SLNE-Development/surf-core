package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.surfapi.core.api.messages.adventure.key
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import java.util.*
import kotlin.time.Duration.Companion.seconds

val authentificationService = AuthentificationService()

class AuthentificationService {
    val authMap = redisApi.createSyncMap<UUID, ByteArray>("surf-core:authentification", 10.seconds)
    val continuations = mutableObject2ObjectMapOf<UUID, Continuation>()
    val lastServers = mutableObject2ObjectMapOf<UUID, String>()
    val key = key("surf-core", "transfer-authentification")
    val lastServerKey = key("surf-core", "last-server")

    fun authenticate(uuid: UUID, token: ByteArray): Boolean {
        println("Authenticating player $uuid")
        val storedToken = authMap.remove(uuid) ?: return false

        println("Stored token: ${storedToken.contentToString()}")

        if (!storedToken.contentEquals(token)) {
            println("Tokens do not match!")
            return false
        }

        println("Tokens match, resuming continuation.")

        continuations[uuid]?.resume()
        continuations.remove(uuid)

        return true
    }

    fun preTransfer(uuid: UUID, token: ByteArray) {
        authMap[uuid] = token
    }

    fun init() = Unit
}