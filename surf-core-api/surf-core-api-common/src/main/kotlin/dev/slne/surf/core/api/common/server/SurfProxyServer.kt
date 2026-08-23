package dev.slne.surf.core.api.common.server

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.ip.inetsocket.SerializableInetSocketAddress
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable

@Serializable
data class SurfProxyServer(
    override val name: String,
    override val displayName: String,
    override val category: String,
    override val uuid: SerializableUUID,
    override val state: SurfServerState,
    override val maxPlayers: Int,
    override val startedAt: SerializableOffsetDateTime,
    val address: SerializableInetSocketAddress
) : CommonSurfServer {
    override fun getPlayers(): ObjectSet<SurfPlayer> {
        val players = mutableObjectSetOf<SurfPlayer>()

        for (player in SurfCoreApi.getOnlinePlayers()) {
            if (player.currentProxyName == name) {
                players.add(player)
            }
        }

        return players.freeze()
    }

    override fun getPlayerCount() =
        SurfCoreApi.getOnlinePlayers().count { it.currentProxyName == name }

    companion object {
        fun current() = SurfCoreApi.getCurrentProxy()
    }
}