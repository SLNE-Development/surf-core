package dev.slne.surf.core.api.common.server

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.surfapi.core.api.serializer.java.ip.inetsocket.SerializableInetSocketAddress
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable

@Serializable
data class SurfServer(
    val name: String,
    val displayName: String,
    val category: String,
    val state: SurfServerState,
    val type: SurfServerType,
    val maxPlayers: Int,
    val externalConnectionAddress: SerializableInetSocketAddress?
) {
    fun getPlayers(): ObjectSet<SurfPlayer> = if (type == SurfServerType.PROXY) {
        surfCoreApi.getOnlinePlayers().filter { it.currentProxy?.name == name }.toObjectSet()
    } else {
        surfCoreApi.getOnlinePlayers().filter { it.currentServer?.name == name }.toObjectSet()
    }

    fun getPlayerCount() = getPlayers().size

    fun sendPlayers(otherServer: SurfServer) = getPlayers().forEach { it.send(otherServer) }
    fun pullPlayers(otherServer: SurfServer) = otherServer.getPlayers().forEach { it.send(this) }
    fun pullPlayers(vararg players: SurfPlayer) = players.forEach { it.send(this) }


    fun isProxy() = type == SurfServerType.PROXY
    fun isBackend() = type == SurfServerType.SERVER

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
