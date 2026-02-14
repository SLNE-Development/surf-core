package dev.slne.surf.core.api.common.server

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.api.common.surfCoreApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@Serializable
sealed interface CommonSurfServer {
    val name: String
    val displayName: String
    val category: String
    val state: SurfServerState
    val maxPlayers: Int

    fun getPlayers(): ObjectSet<SurfPlayer>
    fun getPlayerCount() = getPlayers().size

    fun sendPlayers(otherServer: CommonSurfServer) = getPlayers().forEach { it.send(otherServer) }
    fun pullPlayers(otherServer: CommonSurfServer) =
        otherServer.getPlayers().forEach { it.send(this) }

    fun pullPlayers(vararg players: SurfPlayer) = players.forEach { it.send(this) }

    fun isProxy() = this is SurfProxyServer
    fun isBackend() = this is SurfServer

    companion object {
        operator fun get(name: String) = surfCoreApi.getServerByName(name)
    }
}
