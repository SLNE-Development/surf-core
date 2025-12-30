package dev.slne.surf.core.core.common

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.player.surfPlayerService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

abstract class SurfCoreApiImpl : SurfCoreApi {
    override fun getOnlinePlayers(): ObjectSet<SurfPlayer> = surfPlayerService.players

    override fun getPlayer(name: String) = surfPlayerService.findPlayerByName(name)

    override fun getPlayer(uuid: UUID) = surfPlayerService.findPlayerByUuid(uuid)

    override suspend fun getOfflinePlayer(name: String) =
        surfPlayerService.getOrLoadPlayerByName(name)

    override suspend fun getOfflinePlayer(uuid: UUID) =
        surfPlayerService.getOrLoadPlayerByUuid(uuid)
}