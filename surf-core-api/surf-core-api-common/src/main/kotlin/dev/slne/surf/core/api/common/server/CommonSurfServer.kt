package dev.slne.surf.core.api.common.server

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
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
    }
}
