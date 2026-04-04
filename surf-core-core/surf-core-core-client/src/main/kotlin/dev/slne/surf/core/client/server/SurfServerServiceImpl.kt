package dev.slne.surf.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.server.SurfServerService
import it.unimi.dsi.fastutil.objects.ObjectSet
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

    override fun getServerByName(name: String) = servers.find { it.name == name }
    override fun getServerByCategory(category: String) =
        servers.filter { it.category == category }.toObjectSet()

    override fun getProxyServerByName(name: String) = proxyServers.find { it.name == name }
    override fun getProxyServerByCategory(category: String) =
        proxyServers.filter { it.category == category }.toObjectSet()

    override fun getServerByUuid(uuid: UUID): CommonSurfServer? = servers.find { it.uuid == uuid }
}