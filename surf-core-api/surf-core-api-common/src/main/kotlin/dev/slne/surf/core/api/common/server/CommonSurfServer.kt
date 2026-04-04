package dev.slne.surf.core.api.common.server

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@Serializable
sealed interface CommonSurfServer {
    val name: String
    val displayName: String
    val category: String
    val uuid: SerializableUUID
    val state: SurfServerState
    val maxPlayers: Int

    val startedAt: SerializableOffsetDateTime

    fun getPlayers(): ObjectSet<SurfPlayer>
    fun getPlayerCount() = getPlayers().size

    fun isProxy() = this is SurfProxyServer
    fun isBackend() = this is SurfServer

    companion object {
        operator fun get(name: String) = SurfCoreApi.getCommonServerByName(name)
        fun current(): CommonSurfServer = SurfCoreApi.getCurrentServerName().let {
            SurfCoreApi.getServerByName(it) ?: SurfCoreApi.getProxyServerByName(it)
        }
            ?: error("Current server ${SurfCoreApi.getCurrentServerName()} not found! Are you sure you're running on a Surf server?")
    }
}
