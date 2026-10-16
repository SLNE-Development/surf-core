package dev.slne.surf.core.api.common

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass

private val api = requiredService<SurfCoreApi>()

@Deprecated(
    message = "Use SurfCoreApi instead.",
    replaceWith = ReplaceWith("SurfCoreApi"),
    level = DeprecationLevel.WARNING
)
val surfCoreApi: SurfCoreApi = api

interface SurfCoreApi {
    fun getOnlinePlayers(): ObjectSet<SurfPlayer>
    fun getPlayer(name: String): SurfPlayer?
    fun getPlayer(uuid: UUID): SurfPlayer?

    fun getCurrentServerName(): String
    fun getCurrentServerCategory(): String
    fun getCurrentServerDisplayName(): String
    fun getCurrentServer(): SurfServer
    fun getCurrentProxy(): SurfProxyServer

    fun getServerByName(name: String): SurfServer?
    fun getCommonServerByName(name: String): CommonSurfServer?
    fun getServerByCategory(category: String): ObjectSet<SurfServer>
    fun getServerWithLeastPlayers(category: String): SurfServer?
    fun getProxyServerByName(name: String): SurfProxyServer?
    fun getServers(): ObjectSet<SurfServer>
    fun getProxies(): ObjectSet<SurfProxyServer>
    fun getCommonServers(): ObjectSet<CommonSurfServer>

    fun registerListener(listener: Any)
    fun fireEvent(event: SurfEvent)
    fun subscribe(eventClass: KClass<out SurfEvent>, handler: (SurfEvent) -> Unit)

    suspend fun loadOfflinePlayerNameEntries(): List<OfflinePlayerNameCache.Entry>

    /**
     * Registers a [CoreJoinListener] on this server. It is asked whenever a player is about
     * to be transferred to this server (or proxy).
     */
    fun registerJoinListener(listener: CoreJoinListener)
    fun unregisterJoinListener(listener: CoreJoinListener)

    /**
     * Asks the [server] whether the player with the given [playerUuid] could join it.
     *
     * The target acknowledges the request, runs all of its [CoreJoinListener]s and sends
     * the combined result back. No transfer is performed.
     */
    suspend fun checkJoin(playerUuid: UUID, server: CommonSurfServer): SurfJoinCheckResult

    /**
     * Sends a request to connect the specified player to the given server and awaits the result.
     * This method can only send a player to a backend server, not a proxy.
     *
     * Before connecting, the target server is asked via [checkJoin] whether the player may join.
     * If the check is not allowed, the player is not sent and
     * [SurfServerConnectResult.Status.JOIN_DENIED] is returned.
     */
    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer
    ): SurfServerConnectResult = sendPlayerAwaiting(surfPlayer, surfServer, false)

    /**
     * Like [sendPlayerAwaiting], but with [force] the player is sent even if the join check
     * was not allowed.
     */
    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer,
        force: Boolean
    ): SurfServerConnectResult

    /**
     * Transfers the specified player to the given proxy and awaits the result.
     *
     * Before transferring, the target proxy is asked via [checkJoin] whether the player may join.
     * If the check is not allowed, the player is not transferred and
     * [SurfProxyServerConnectionResult.Status.JOIN_DENIED] is returned.
     */
    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfProxyServer: SurfProxyServer
    ): SurfProxyServerConnectionResult = sendPlayerAwaiting(surfPlayer, surfProxyServer, false)

    /**
     * Like [sendPlayerAwaiting], but with [force] the player is transferred even if the join
     * check was not allowed.
     */
    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfProxyServer: SurfProxyServer,
        force: Boolean
    ): SurfProxyServerConnectionResult

    /**
     * Sends a text message to the given player via the cross-server messaging system.
     *
     * This method publishes the message through Redis so that it reaches the player
     * regardless of which server in the network they are currently connected to.
     * The delivery is asynchronous and is not limited to the local server instance.
     *
     * @param player the target player, which may be connected to any server in the network
     * @param text the message component to send to the player
     */
    fun sendText(player: SurfPlayer, text: Component)

    suspend fun getOfflinePlayer(name: String): SurfPlayer?
    suspend fun getOfflinePlayer(uuid: UUID): SurfPlayer?

    companion object : SurfCoreApi by api {
        val INSTANCE get() = api
    }
}