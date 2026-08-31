package dev.slne.surf.core.core.common.player

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.player.SurfPlayer
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

private val service = requiredService<SurfPlayerService>()

interface SurfPlayerService {
    val players: @UnmodifiableView ObjectSet<SurfPlayer>

    fun findPlayerByName(name: String): SurfPlayer?
    fun findPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun findPlayerByUuidRemote(uuid: UUID): SurfPlayer?

    suspend fun loadPlayerByName(name: String): SurfPlayer?
    suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun getOrLoadPlayerByName(name: String): SurfPlayer?
    suspend fun getOrLoadPlayerByUuid(uuid: UUID): SurfPlayer?

    suspend fun getOrLoadOrCreatePlayerByUuid(uuid: UUID): SurfPlayer

    suspend fun savePlayer(player: SurfPlayer)

    /**
     * Number of cached players per [SurfPlayer.currentServerName], players without a server skipped.
     */
    fun playerCountsByServer(): Object2IntMap<String>

    /**
     * Number of cached players per [SurfPlayer.currentProxyName], players without a proxy skipped.
     */
    fun playerCountsByProxy(): Object2IntMap<String>

    fun clearPlayers()

    @Deprecated("Use cachePlayerAndAwait instead", ReplaceWith("cachePlayerAndAwait(player)"))
    fun cachePlayer(player: SurfPlayer)
    suspend fun cachePlayerAndAwait(player: SurfPlayer)
    
    @Deprecated("Use invalidatePlayerIfEqualsAndAwait instead", ReplaceWith("invalidatePlayerIfEqualsAndAwait(player)"))
    fun invalidatePlayer(uuid: UUID)

    suspend fun replacePlayerIfEqualsAndAwait(expected: SurfPlayer, updated: SurfPlayer): Boolean
    suspend fun invalidatePlayerIfEqualsAndAwait(player: SurfPlayer): Boolean

    companion object : SurfPlayerService by service {
        val INSTANCE get() = service
    }
}