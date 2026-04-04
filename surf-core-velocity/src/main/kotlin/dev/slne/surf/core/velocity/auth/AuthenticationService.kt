package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.core.core.CoreInstance
import java.security.MessageDigest
import java.util.*
import kotlin.time.Duration.Companion.seconds

object AuthenticationService {
    val authMap = CoreInstance.redisApi.createSyncMap<UUID, ByteArray>(
        "surf-core:authentification",
        5.seconds
    )

    val lastServerMap = CoreInstance.redisApi.createSyncMap<UUID, String>(
        "surf-core:last-server",
        5.seconds
    )

    val continuations = mutableObject2ObjectMapOf<UUID, Continuation>()
    val key = key("surf-core", "transfer-authentification")

    fun authenticate(uuid: UUID, token: ByteArray): Boolean {
        val storedHash = authMap.remove(uuid)

        if (storedHash == null) {
            println("[connection] Failed to authenticate player $uuid: no stored hash")
            return false
        }

        val tokenHash = hash(token)

        if (!storedHash.contentEquals(tokenHash)) {
            println("[connection] Failed to authenticate player $uuid: invalid token")
            return false
        }

        continuations[uuid]?.resume()
        continuations.remove(uuid)

        return true
    }

    fun preTransfer(uuid: UUID, token: ByteArray) {
        authMap[uuid] = hash(token)
    }

    private fun hash(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    fun init() = Unit
}
