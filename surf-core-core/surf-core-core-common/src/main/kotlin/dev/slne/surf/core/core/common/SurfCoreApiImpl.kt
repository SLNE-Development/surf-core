package dev.slne.surf.core.core.common

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.event.SurfEvent
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.server.connection.SurfServerConnectResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.join.CoreJoinListenerRegistry
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.event.SurfPlayerMessageRedisEvent
import dev.slne.surf.core.core.common.redis.request.PlayerJoinCheckRequest
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.request.SendPlayerToServerRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerJoinCheckResultWatcher
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.core.common.redis.watcher.PlayerServerConnectionResultWatcher
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.redis.request.RequestTimeoutException
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

private val SERVER_CONNECT_RESULT_TIMEOUT = 45.seconds
private val JOIN_CHECK_RESULT_TIMEOUT = 15.seconds

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

    override fun getServerWithLeastPlayers(category: String): SurfServer? {
        val counts = SurfPlayerService.playerCountsByServer()
        return SurfServerService.getServerByCategory(category)
            .minByOrNull { counts.getInt(it.name) }
    }

    override fun getServers() = SurfServerService.servers
    override fun getProxies() = SurfServerService.proxyServers

    override fun sendText(player: SurfPlayer, text: Component) {
        CoreInstance.redisApi.publishEvent(SurfPlayerMessageRedisEvent(player.uuid, text))
    }

    override fun subscribe(eventClass: KClass<out SurfEvent>, handler: (SurfEvent) -> Unit) =
        SurfEventBus.subscribe(eventClass, handler)

    override fun registerListener(listener: Any) = SurfEventBus.registerListener(listener)
    override fun fireEvent(event: SurfEvent) = SurfEventBus.fire(event)

    override fun registerJoinListener(listener: CoreJoinListener) =
        CoreJoinListenerRegistry.register(listener)

    override fun unregisterJoinListener(listener: CoreJoinListener) =
        CoreJoinListenerRegistry.unregister(listener)

    override suspend fun checkJoin(playerUuid: UUID, server: CommonSurfServer) =
        checkJoin(playerUuid, server, false)

    private suspend fun checkJoin(
        playerUuid: UUID,
        server: CommonSurfServer,
        force: Boolean
    ): SurfJoinCheckResult {
        if (server.name == getCurrentServerName()) {
            return CoreJoinListenerRegistry.check(playerUuid)
        }

        val requestId = UUID.randomUUID()
        val awaitingResult = PlayerJoinCheckResultWatcher.watch(requestId)

        try {
            PlayerJoinCheckRequest.createRequest(playerUuid, server.name, requestId, force)
        } catch (_: RequestTimeoutException) {
            PlayerJoinCheckResultWatcher.complete(requestId, SurfJoinCheckResult.TARGET_UNREACHABLE)
        }

        return withTimeoutOrNull(JOIN_CHECK_RESULT_TIMEOUT) {
            awaitingResult.await()
        } ?: SurfJoinCheckResult.TIMEOUT.also {
            PlayerJoinCheckResultWatcher.complete(requestId, it)
        }
    }

    override suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfServer: SurfServer,
        force: Boolean
    ): SurfServerConnectResult {
        val joinCheck = checkJoin(surfPlayer.uuid, surfServer, force)

        if (!joinCheck.isAllowed() && !force) {
            return SurfServerConnectResult(
                SurfServerConnectResult.Status.JOIN_DENIED,
                null,
                joinCheck
            )
        }

        val result = connectPlayer(surfPlayer, surfServer)
        return SurfServerConnectResult(result.status, result.velocityMessage, joinCheck)
    }

    /**
     * Connects the player to the server without performing a join check.
     */
    protected open suspend fun connectPlayer(
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

        return withTimeoutOrNull(SERVER_CONNECT_RESULT_TIMEOUT) {
            awaitingResult.await()
        }
            ?: SurfServerConnectResult(SurfServerConnectResult.Status.UNKNOWN_ERROR, null).also {
                PlayerServerConnectionResultWatcher.complete(requestId, it)
            }
    }

    override suspend fun sendPlayerAwaiting(
        surfPlayer: SurfPlayer,
        surfProxyServer: SurfProxyServer,
        force: Boolean
    ): SurfProxyServerConnectionResult {
        val playerUuid = surfPlayer.uuid

        if (surfPlayer.currentProxyName == surfProxyServer.name) {
            return SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.ALREADY_CONNECTED)
        }

        val joinCheck = checkJoin(playerUuid, surfProxyServer, force)

        if (!joinCheck.isAllowed() && !force) {
            return SurfProxyServerConnectionResult(
                SurfProxyServerConnectionResult.Status.JOIN_DENIED,
                joinCheck
            )
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

        val result = withTimeoutOrNull(15.seconds) {
            awaitingResult.await().also {
                PlayerProxyConnectionResultWatcher.cleanUp(playerUuid)
            }
        } ?: run {
            PlayerProxyConnectionResultWatcher.cleanUp(playerUuid)
            SurfProxyServerConnectionResult(
                SurfProxyServerConnectionResult.Status.ERR_UNKNOWN
            )
        }

        return SurfProxyServerConnectionResult(result.status, joinCheck)
    }
}
