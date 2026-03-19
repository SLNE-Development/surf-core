package dev.slne.surf.core.api.common.server

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.surfapi.core.api.serializer.java.ip.inetsocket.SerializableInetSocketAddress
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable

@Serializable
data class SurfProxyServer(
    override val name: String,
    override val displayName: String,
    override val category: String,
    override val state: SurfServerState,
    override val maxPlayers: Int,
    val address: SerializableInetSocketAddress
) : CommonSurfServer {
    override fun getPlayers(): ObjectSet<SurfPlayer> =
        SurfCoreApi.getOnlinePlayers().filter { it.currentProxy?.name == name }.toObjectSet()

    companion object {
        fun current() = SurfCoreApi.getCurrentProxy()
    }
}