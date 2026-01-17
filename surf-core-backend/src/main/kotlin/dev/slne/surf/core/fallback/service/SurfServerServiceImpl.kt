package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services

@AutoService(SurfServerService::class)
class SurfServerServiceImpl : SurfServerService, Services.Fallback {
    val globalServers = redisApi.createSyncMap<String, SurfServer>("surf-core:servers")

    override val servers get() = globalServers.snapshot().values.toObjectSet()
    override fun addServer(server: SurfServer) {
        globalServers.put(server.name, server)
    }

    override fun removeServer(server: SurfServer) {
        globalServers.remove(server.name)
    }

    override fun changeState(
        surfServer: SurfServer,
        state: SurfServerState
    ) {
        globalServers.put(surfServer.name, surfServer.copy(state = state))
    }

    override fun getServerByName(name: String): SurfServer? {
        return servers.find { it.name == name }
    }

    override fun getServerByCategory(category: String): ObjectSet<SurfServer> {
        return servers.filter { it.category == category }.toObjectSet()
    }

    override fun init() {}
}