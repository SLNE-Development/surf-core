package dev.slne.surf.core.core.common

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.request.SendPlayerToServerRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.core.common.redis.watcher.PlayerServerConnectionResultWatcher
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.redis.request.RequestTimeoutException
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

abstract class SurfCoreApiImpl : SurfCoreApi {
    override fun getOnlinePlayers(): ObjectSet<SurfPlayer> = SurfPlayerService.players
    override fun getPlayer(name: String) = SurfPlayerService.findPlayerByName(name)
    override fun getPlayer(uuid: UUID) = SurfPlayerService.findPlayerByUuid(uuid)
    override suspend fun getOfflinePlayer(name: String) =
        SurfPlayerService.getOrLoadPlayerByName(name)

    override suspend fun getOfflinePlayer(uuid: UUID) =
        SurfPlayerService.getOrLoadPlayerByUuid(uuid)

    override fun getCurrentServer() = SurfServerService.getServerByName(getCurrentServerName())
        ?: error("Current server ${getCurrentServerName()} not found! Are you sure you're running on a backend server?")

    override fun getCurrentProxy() = SurfServerService.getProxyServerByName(getCurrentServerName())
        ?: error("Current proxy ${getCurrentServerName()} not found! Are you sure you're running on a proxy server?")

    override fun getServerByName(name: String) = SurfServerService.getServerByName(name)
    override fun getCommonServerByName(name: String) =
        SurfServerService.getServerByName(name) ?: SurfServerService.getProxyServerByName(name)

    override fun getCommonServers(): ObjectSet<CommonSurfServer> {
        val commonServers = mutableObjectSetOf<CommonSurfServer>()
        commonServers.addAll(SurfServerService.servers)
        commonServers.addAll(SurfServerService.proxyServers)
        return commonServers
    }

    override fun getProxyServerByName(name: String) = SurfServerService.getProxyServerByName(name)
    override fun getServerByCategory(category: String) =
        SurfServerService.getServerByCategory(category)

    override fun getServerWithLeastPlayers(category: String) =
        SurfServerService.getServerByCategory(category).minByOrNull { it.getPlayerCount() }

    override fun getServers() = SurfServerService.servers
    override fun getProxies() = SurfServerService.proxyServers

    override fun sendText(player: SurfPlayer, text: Component) {
        CoreInstance.redisApi.publishEvent(SurfPlayerMessageRedisEvent(player.uuid, text))
    }

    override fun subscribe(eventClass: KClass<out SurfEvent>, handler: (SurfEvent) -> Unit) =
        SurfEventBus.subscribe(eventClass, handler)

    override fun registerListener(listener: Any) = SurfEventBus.registerListener(listener)
    override fun fireEvent(event: SurfEvent) = SurfEventBus.fire(event)

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