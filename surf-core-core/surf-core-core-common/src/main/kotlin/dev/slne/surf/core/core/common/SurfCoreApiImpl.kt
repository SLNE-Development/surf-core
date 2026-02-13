package dev.slne.surf.core.core.common

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.redis.PlayerConnectionResultWatcher
import dev.slne.surf.core.core.common.redis.SendPlayerToServerRequest
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.redis.request.RequestTimeoutException
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass

abstract class SurfCoreApiImpl : SurfCoreApi {
    override fun getOnlinePlayers(): ObjectSet<SurfPlayer> = surfPlayerService.players

    override fun getPlayer(name: String) = surfPlayerService.findPlayerByName(name)

    override fun getPlayer(uuid: UUID) = surfPlayerService.findPlayerByUuid(uuid)

    override suspend fun getOfflinePlayer(name: String) =
        surfPlayerService.getOrLoadPlayerByName(name)

    override suspend fun getOfflinePlayer(uuid: UUID) =
        surfPlayerService.getOrLoadPlayerByUuid(uuid)

    override fun getCurrentServer(): SurfServer {
        return surfServerService.getServerByName(getCurrentServerName())
            ?: error("Current server ${getCurrentServerName()} not found")
    }

    override fun getServerByName(name: String): SurfServer? {
        return surfServerService.getServerByName(name)
    }

    override fun getServerByCategory(category: String): ObjectSet<SurfServer> {
        return surfServerService.getServerByCategory(category)
    }

    override fun getServers(): ObjectSet<SurfServer> {
        return surfServerService.servers
    }

    override fun sendText(player: SurfPlayer, text: Component) {
        redisApi.publishEvent(SurfPlayerMessageRedisEvent(player.uuid, text))
    }

    override fun subscribe(eventClass: KClass<out SurfEvent>, handler: (SurfEvent) -> Unit) {
        surfEventBus.subscribe(eventClass, handler)
    }

    override fun registerListener(listener: Any) {
        surfEventBus.registerListener(listener)
    }

    override fun fireEvent(event: SurfEvent) {
        surfEventBus.fire(event)
    }

    override suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer
    ): SurfServerConnectResult {
        val requestId = UUID.randomUUID()
        val awaitingResult = PlayerConnectionResultWatcher.watch(requestId)
        try {
            SendPlayerToServerRequest.createRequest(surfPlayer, surfServer, requestId)
        } catch (_: RequestTimeoutException) {
            PlayerConnectionResultWatcher.complete(
                requestId,
                SurfServerConnectResult(SurfServerConnectResult.Status.UNKNOWN_ERROR, null)
            )
        }

        return awaitingResult.await()
    }
}