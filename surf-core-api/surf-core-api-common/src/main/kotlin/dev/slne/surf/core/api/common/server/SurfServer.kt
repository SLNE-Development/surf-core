package dev.slne.surf.core.api.common.server

import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.surfapi.core.api.serializer.java.ip.inetsocket.SerializableInetSocketAddress
import kotlinx.serialization.Serializable

@Serializable
data class SurfServer(
    val name: String,
    val category: String,
    val state: SurfServerState,
    val type: SurfServerType,
    val connectionAddress: SerializableInetSocketAddress
) {
    fun getPlayers() = surfCoreApi.getOnlinePlayers().filter { it.currentServer == this }
    fun getPlayerCount() = getPlayers().size

    companion object {
        /**
         * Gets the current server instance.
         * @return The current [SurfServer] instance.
         *
         * On velocity, this returns the proxy server, on paper, this returns the backend instance.
         */
        fun current() = surfCoreApi.getCurrentServer()


        operator fun get(name: String) = surfCoreApi.getServerByName(name)
    }
}
