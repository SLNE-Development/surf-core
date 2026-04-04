package dev.slne.surf.core.core.common.server

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

private val service = requiredService<SurfServerService>()

interface SurfServerService {
    val servers: @UnmodifiableView ObjectSet<SurfServer>
    val proxyServers: @UnmodifiableView ObjectSet<SurfProxyServer>

    fun addServer(server: CommonSurfServer)
    fun removeServer(server: CommonSurfServer)
    fun changeState(commonSurfServer: CommonSurfServer, state: SurfServerState)

    fun getServerByName(name: String): SurfServer?
    fun getServerByUuid(uuid: UUID): CommonSurfServer?
    fun getServerByCategory(category: String): ObjectSet<SurfServer>
    fun getProxyServerByName(name: String): SurfProxyServer?
    fun getProxyServerByCategory(category: String): ObjectSet<SurfProxyServer>

    companion object : SurfServerService by service {
        val INSTANCE get() = service
    }
}