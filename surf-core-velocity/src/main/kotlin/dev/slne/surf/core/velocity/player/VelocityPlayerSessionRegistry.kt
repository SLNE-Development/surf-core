package dev.slne.surf.core.velocity.player

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.core.api.common.player.SurfPlayer
import java.util.concurrent.ConcurrentHashMap

object VelocityPlayerSessionRegistry {

    private val sessions = ConcurrentHashMap<PlayerKey, SurfPlayer>()

    fun register(
        player: Player,
        surfPlayer: SurfPlayer,
    ) {
        requireNotNull(surfPlayer.connectionSessionId) {
            "Cannot register a SurfPlayer without a connection session id"
        }

        sessions[PlayerKey(player)] = surfPlayer
    }

    operator fun get(player: Player): SurfPlayer? {
        return sessions[PlayerKey(player)]
    }

    fun replace(
        player: Player,
        expected: SurfPlayer,
        updated: SurfPlayer,
    ): Boolean {
        return sessions.replace(
            PlayerKey(player),
            expected,
            updated,
        )
    }

    fun remove(player: Player): SurfPlayer? {
        return sessions.remove(PlayerKey(player))
    }

    private class PlayerKey(
        private val player: Player,
    ) {
        override fun equals(other: Any?): Boolean {
            return other is PlayerKey && player === other.player
        }

        override fun hashCode(): Int {
            return System.identityHashCode(player)
        }
    }
}