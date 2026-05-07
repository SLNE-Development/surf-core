package dev.slne.surf.core.api.paper

import dev.slne.surf.core.api.paper.CorePlayerInfoProvider.Companion.getServerInfo
import java.util.*

interface CorePlayerInfoProvider {
    fun createServerInfo(playerUuid: UUID): ServerInfo?

    /**
     * Server Info data class containing the server name, nice name and region. This is used to provide information about the server a player is currently on.
     *
     * @param serverName The name of the server the player is currently on.
     * @param serverDisplayName The nice name of the server the player is currently on.
     * @param serverRegion The region of the server the player is currently on.
     *
     * NOTE: This method should not be used to gain access to players current server. Use [dev.slne.surf.core.api.common.player.SurfPlayer.currentServer] instead.
     *
     * @see [getServerInfo]
     */
    data class ServerInfo(
        val serverName: String,
        val serverDisplayName: String,
        val serverRegion: String
    )

    companion object {
        private lateinit var instance: CorePlayerInfoProvider

        fun setInstance(provider: CorePlayerInfoProvider) {
            instance = provider
        }

        /**
         * Available after plugin bootstrapper
         */
        fun getServerInfo(playerUuid: UUID) = instance.createServerInfo(playerUuid)
    }
}