package dev.slne.surf.core.core.common

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.player.surfPlayerService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfCoreApi::class)
class SurfCoreApiImpl : SurfCoreApi, Services.Fallback {
    override fun getOnlinePlayers(): ObjectSet<SurfPlayer> = surfPlayerService.players

    override fun getPlayer(name: String) = surfPlayerService.findPlayerByName(name)

    override fun getPlayer(uuid: UUID) = surfPlayerService.findPlayerByUuid(uuid)

    override suspend fun getOfflinePlayer(name: String) =
        surfPlayerService.getOrLoadPlayerByName(name)
}