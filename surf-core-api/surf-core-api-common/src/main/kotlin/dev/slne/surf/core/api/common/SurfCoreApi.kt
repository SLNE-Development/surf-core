package dev.slne.surf.core.api.common

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

val surfCoreApi = requiredService<SurfCoreApi>()

interface SurfCoreApi {
    fun getOnlinePlayers(): ObjectSet<SurfPlayer>
    fun getPlayer(name: String): SurfPlayer?
    fun getPlayer(uuid: UUID): SurfPlayer?

    fun getCurrentServerName(): String

    suspend fun getOfflinePlayer(name: String): SurfPlayer?
    suspend fun getOfflinePlayer(uuid: UUID): SurfPlayer?
}