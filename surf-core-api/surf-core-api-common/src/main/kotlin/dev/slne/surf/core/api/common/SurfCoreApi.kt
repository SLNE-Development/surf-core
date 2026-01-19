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

    fun sendText(player: SurfPlayer, text: Component)

    suspend fun getOfflinePlayer(name: String): SurfPlayer?
    suspend fun getOfflinePlayer(uuid: UUID): SurfPlayer?
}