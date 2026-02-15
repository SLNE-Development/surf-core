package dev.slne.surf.core.api.common

import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass

val surfCoreApi = requiredService<SurfCoreApi>()

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

    fun sendPlayer(player: SurfPlayer, server: CommonSurfServer)

    /**
     * Sends a request to connect the specified player to the given server and awaits the result.
     * This method can only send a player to a backend server, not a proxy.
     */
    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer
    ): SurfServerConnectResult

    suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfProxyServer: SurfProxyServer
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
}