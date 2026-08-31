package dev.slne.surf.core.velocity.auth

import com.google.common.hash.Hashing
import com.velocitypowered.api.event.Continuation
import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.core.CoreInstance
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

object AuthenticationService {
    private val log = logger()

    val authMap = CoreInstance.redisApi.createSyncMap<UUID, ByteArray>(
        "surf-core:authentification",
        5.seconds
    )

    val lastServerMap = CoreInstance.redisApi.createSyncMap<UUID, String>(
        "surf-core:last-server",
        5.seconds
    )

    val continuations = ConcurrentHashMap<UUID, Continuation>()
    val key = key("surf-core", "transfer-authentification")

    suspend fun authenticate(uuid: UUID, token: ByteArray): Boolean {
        val storedHash = authMap.getRemote(uuid)

        if (storedHash == null) {
            log.atInfo()
                .log("Failed to authenticate player %s: no stored hash", uuid)
            return false
        }

        if (!authMap.removeIfEqualsAndAwait(uuid, storedHash)) {
            log.atInfo()
                .log("Failed to authenticate player %s: authentication state changed", uuid)
            return false
        }

        val tokenHash = hash(token)

        if (!storedHash.contentEquals(tokenHash)) {
            log.atInfo()
                .log("Failed to authenticate player %s: invalid token", uuid)
            return false
        }

        continuations.remove(uuid)?.resume()

        return true
    }

    suspend fun preTransfer(uuid: UUID, token: ByteArray) {
        authMap.putAndAwait(uuid, hash(token))
    }

    private fun hash(data: ByteArray): ByteArray {
        return Hashing.sha256().hashBytes(data).asBytes()
    }

    fun init() = Unit
}
