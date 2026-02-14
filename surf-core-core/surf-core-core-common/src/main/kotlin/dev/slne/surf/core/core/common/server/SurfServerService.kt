package dev.slne.surf.core.core.common.server

import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet

val surfServerService = requiredService<SurfServerService>()

interface SurfServerService {
    val servers: ObjectSet<SurfServer>
    val proxyServers: ObjectSet<SurfProxyServer>

    fun addServer(server: CommonSurfServer)
    fun removeServer(server: CommonSurfServer)
    fun changeState(commonSurfServer: CommonSurfServer, state: SurfServerState)

    fun getServerByName(name: String): SurfServer?
    fun getServerByCategory(category: String): ObjectSet<SurfServer>
    fun getProxyServerByName(name: String): SurfProxyServer?
    fun getProxyServerByCategory(category: String): ObjectSet<SurfProxyServer>

    fun init()
}