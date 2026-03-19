package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services

@AutoService(SurfServerService::class)
class SurfServerServiceImpl : SurfServerService, Services.Fallback {
    val globalServers = redisApi.createSyncMap<String, SurfServer>("surf-core:surf-servers")
    val globalProxies =
        redisApi.createSyncMap<String, SurfProxyServer>("surf-core:surf-server-proxies")

    override val servers get() = globalServers.snapshot().values.toObjectSet()
    override val proxyServers: ObjectSet<SurfProxyServer> get() = globalProxies.snapshot().values.toObjectSet()

    override fun addServer(server: CommonSurfServer) {
        when (server) {
            is SurfProxyServer -> globalProxies.put(server.name, server)
            is SurfServer -> globalServers.put(server.name, server)
        }
    }

    override fun removeServer(server: CommonSurfServer) {
        when (server) {
            is SurfProxyServer -> globalProxies.remove(server.name)
            is SurfServer -> globalServers.remove(server.name)
        }
    }

    override fun changeState(
        commonSurfServer: CommonSurfServer,
        state: SurfServerState
    ) {
        val server = when (commonSurfServer) {
            is SurfProxyServer -> globalProxies[commonSurfServer.name]
            is SurfServer -> globalServers[commonSurfServer.name]
            else -> error("Unknown server type: ${commonSurfServer::class}")
        } ?: error("Server ${commonSurfServer.name} not found")

        val updatedServer = when (server) {
            is SurfProxyServer -> server.copy(state = state)
            is SurfServer -> server.copy(state = state)
            else -> error("Unknown server type: ${server::class}")
        }

        when (updatedServer) {
            is SurfProxyServer -> globalProxies.put(updatedServer.name, updatedServer)
            is SurfServer -> globalServers.put(updatedServer.name, updatedServer)
        }
    }

    override fun getServerByName(name: String): SurfServer? {
        return servers.find { it.name == name }
    }

    override fun getServerByCategory(category: String): ObjectSet<SurfServer> {
        return servers.filter { it.category == category }.toObjectSet()
    }

    override fun getProxyServerByName(name: String): SurfProxyServer? {
        return proxyServers.find { it.name == name }
    }

    override fun getProxyServerByCategory(category: String): ObjectSet<SurfProxyServer> {
        return proxyServers.filter { it.category == category }.toObjectSet()
    }

    override fun init() {}
}