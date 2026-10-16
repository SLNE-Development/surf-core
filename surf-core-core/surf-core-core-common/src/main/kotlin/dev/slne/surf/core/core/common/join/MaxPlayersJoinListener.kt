package dev.slne.surf.core.core.common.join

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import dev.slne.surf.core.core.common.permission.CorePermissions
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

abstract class MaxPlayersJoinListener : CoreJoinListener {
    private val log = logger()

    override val name = "max-players"

    protected abstract fun maxPlayers(): Int?
    protected abstract fun playerCount(): Int
    protected abstract fun isConnected(playerUuid: UUID): Boolean

    override suspend fun checkJoin(playerUuid: UUID): CoreJoinResult {
        if (isConnected(playerUuid)) {
            return CoreJoinResult.allowed()
        }

        val maxPlayers = maxPlayers()?.takeIf { it > 0 } ?: return CoreJoinResult.allowed()
        val playerCount = playerCount()
        if (playerCount < maxPlayers) {
            return CoreJoinResult.allowed()
        }

        if (hasBypassPermission(playerUuid)) {
            return CoreJoinResult.allowed()
        }

        return CoreJoinResult.denied(Component.text("Der Server ist voll!"))
    }

    private suspend fun hasBypassPermission(playerUuid: UUID) = try {
        val user = LuckPermsAccess.getUser(playerUuid) ?: LuckPermsAccess.loadUser(playerUuid)

        user.cachedData
            .permissionData
            .checkPermission(CorePermissions.BYPASS_MAX_PLAYERS)
            .asBoolean()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.atWarning()
            .withCause(e)
            .log("Failed to check max players bypass permission for %s", playerUuid)
        false
    }
}
