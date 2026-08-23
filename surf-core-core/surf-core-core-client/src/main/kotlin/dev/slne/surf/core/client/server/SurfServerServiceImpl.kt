package dev.slne.surf.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.redis.request.ExecuteCommandServerRequest
import dev.slne.surf.core.core.common.redis.request.ShutdownServerRequest
import dev.slne.surf.core.core.common.server.SurfServerService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import java.util.*

@AutoService(SurfServerService::class)
class SurfServerServiceImpl : SurfServerService {
    private val _servers =
        ClientCoreInstance.redisApi.createSyncMap<String, SurfServer>("surf-core:surf-servers")
    private val _proxies =
        ClientCoreInstance.redisApi.createSyncMap<String, SurfProxyServer>("surf-core:surf-server-proxies")

    override val servers get() = _servers.snapshot().values.toObjectSet()
    override val proxyServers: ObjectSet<SurfProxyServer> get() = _proxies.snapshot().values.toObjectSet()

    override fun addServer(server: CommonSurfServer) {
        when (server) {
            is SurfProxyServer -> _proxies.put(server.name, server)
            is SurfServer -> _servers.put(server.name, server)
        }
    }

    override fun removeServer(server: CommonSurfServer) {
        when (server) {
            is SurfProxyServer -> _proxies.remove(server.name)
            is SurfServer -> _servers.remove(server.name)
        }
    }

    override fun changeState(
        commonSurfServer: CommonSurfServer,
        state: SurfServerState
    ) {
        val server = when (commonSurfServer) {
            is SurfProxyServer -> _proxies[commonSurfServer.name]
            is SurfServer -> _servers[commonSurfServer.name]
        } ?: error("Server ${commonSurfServer.name} not found")

        val updatedServer = when (server) {
            is SurfProxyServer -> server.copy(state = state)
            is SurfServer -> server.copy(state = state)
        }

        when (updatedServer) {
            is SurfProxyServer -> _proxies.put(updatedServer.name, updatedServer)
            is SurfServer -> _servers.put(updatedServer.name, updatedServer)
        }
    }

    override suspend fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?) =
        ShutdownServerRequest.createRequest(commonSurfServer, reason).status

    override suspend fun executeCommand(
        commonSurfServer: CommonSurfServer,
        command: String
    ) = ExecuteCommandServerRequest.createRequest(commonSurfServer, command).status

    override fun getServerByName(name: String) = _servers[name]
    override fun getProxyServerByName(name: String) = _proxies[name]

    override fun getServerByCategory(category: String): ObjectSet<SurfServer> {
        val matches = mutableObjectSetOf<SurfServer>()

        for (server in _servers.snapshot().values) {
            if (server.category == category) {
                matches.add(server)
            }
        }

        return matches.freeze()
    }

    override fun getProxyServerByCategory(category: String): ObjectSet<SurfProxyServer> {
        val matches = mutableObjectSetOf<SurfProxyServer>()

        for (proxy in _proxies.snapshot().values) {
            if (proxy.category == category) {
                matches.add(proxy)
            }
        }

        return matches.freeze()
    }

    override fun getServerByUuid(uuid: UUID): CommonSurfServer? {
        for (server in _servers.snapshot().values) {
            if (server.uuid == uuid) {
                return server
            }
        }

        return null
    }
}
