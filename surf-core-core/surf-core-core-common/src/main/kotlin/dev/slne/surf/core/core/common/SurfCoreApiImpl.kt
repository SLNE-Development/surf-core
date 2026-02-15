package dev.slne.surf.core.core.common

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.request.SendPlayerToServerRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.core.common.redis.watcher.PlayerServerConnectionResultWatcher
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.redis.request.RequestTimeoutException
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

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
            ?: error("Current server ${getCurrentServerName()} not found! Are you sure you're running on a backend server?")
    }

    override fun getCurrentProxy(): SurfProxyServer {
        return surfServerService.getProxyServerByName(getCurrentServerName())
            ?: error("Current proxy ${getCurrentServerName()} not found! Are you sure you're running on a proxy server?")
    }

    override fun getServerByName(name: String): SurfServer? {
        return surfServerService.getServerByName(name)
    }

    override fun getCommonServerByName(name: String): CommonSurfServer? {
        return surfServerService.getServerByName(name) ?: surfServerService.getProxyServerByName(
            name
        )
    }

    override fun getCommonServers(): ObjectSet<CommonSurfServer> {
        val commonServers = mutableObjectSetOf<CommonSurfServer>()
        commonServers.addAll(surfServerService.servers)
        commonServers.addAll(surfServerService.proxyServers)
        return commonServers
    }

    override fun getProxyServerByName(name: String): SurfProxyServer? {
        return surfServerService.getProxyServerByName(name)
    }

    override fun getServerByCategory(category: String): ObjectSet<SurfServer> {
        return surfServerService.getServerByCategory(category)
    }

    override fun getServerWithLeastPlayers(category: String): SurfServer? {
        return surfServerService.getServerByCategory(category).minByOrNull { it.getPlayerCount() }
    }

    override fun getServers(): ObjectSet<SurfServer> {
        return surfServerService.servers
    }

    override fun getProxies(): ObjectSet<SurfProxyServer> {
        return surfServerService.proxyServers
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
        val awaitingResult = PlayerServerConnectionResultWatcher.watch(requestId)
        try {
            SendPlayerToServerRequest.createRequest(surfPlayer, surfServer, requestId)
        } catch (_: RequestTimeoutException) {
            PlayerServerConnectionResultWatcher.complete(
                requestId,
                SurfServerConnectResult(SurfServerConnectResult.Status.UNKNOWN_ERROR, null)
            )
        }

        return awaitingResult.await()
    }

    override suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfProxyServer: SurfProxyServer
    ): SurfProxyServerConnectionResult {
        val playerUuid = surfPlayer.uuid

        if (surfPlayer.currentProxy?.name == surfProxyServer.name) {
            return SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.ALREADY_CONNECTED)
        }

        val awaitingResult = PlayerProxyConnectionResultWatcher.watch(playerUuid)

        if (!awaitingResult.isCompleted) {
            try {
                SendPlayerToProxyRequest.createRequest(surfPlayer, surfProxyServer, playerUuid)
            } catch (_: RequestTimeoutException) {
                PlayerProxyConnectionResultWatcher.complete(
                    playerUuid,
                    SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.ERR_UNKNOWN)
                )
                PlayerProxyConnectionResultWatcher.cleanUp(playerUuid)
            }
        }

        return withTimeoutOrNull(15.seconds) {
            awaitingResult.await().also {
                PlayerProxyConnectionResultWatcher.cleanUp(playerUuid)
            }
        } ?: run {
            PlayerProxyConnectionResultWatcher.cleanUp(playerUuid)
            SurfProxyServerConnectionResult(
                SurfProxyServerConnectionResult.Status.ERR_UNKNOWN
            )
        }
    }
}