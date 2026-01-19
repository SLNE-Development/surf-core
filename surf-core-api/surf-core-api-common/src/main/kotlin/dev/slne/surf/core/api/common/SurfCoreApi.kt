package dev.slne.surf.core.api.common

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import java.util.*

val surfCoreApi = requiredService<SurfCoreApi>()

interface SurfCoreApi {
    fun getOnlinePlayers(): ObjectSet<SurfPlayer>
    fun getPlayer(name: String): SurfPlayer?
    fun getPlayer(uuid: UUID): SurfPlayer?

    fun getCurrentServerName(): String
    fun getCurrentServerCategory(): String
    fun getCurrentServer(): SurfServer

    fun getServerByName(name: String): SurfServer?
    fun getServerByCategory(category: String): ObjectSet<SurfServer>
    fun getServers(): ObjectSet<SurfServer>

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