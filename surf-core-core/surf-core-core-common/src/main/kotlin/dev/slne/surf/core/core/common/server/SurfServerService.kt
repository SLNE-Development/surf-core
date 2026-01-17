package dev.slne.surf.core.core.common.server

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet

val surfServerService = requiredService<SurfServerService>()

interface SurfServerService {
    val servers: ObjectSet<SurfServer>

    fun addServer(server: SurfServer)
    fun removeServer(server: SurfServer)
    fun changeState(surfServer: SurfServer, state: SurfServerState)

    fun getServerByName(name: String): SurfServer?
    fun getServerByCategory(category: String): ObjectSet<SurfServer>

    fun init()
}