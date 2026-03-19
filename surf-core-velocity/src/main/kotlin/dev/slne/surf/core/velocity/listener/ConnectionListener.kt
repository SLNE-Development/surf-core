package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.util.GameProfile
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.history.SurfPlayerIpAddressHistoryService
import dev.slne.surf.core.core.common.player.history.SurfPlayerNameHistoryService
import dev.slne.surf.core.core.common.player.history.SurfPlayerTextureHistoryService
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.velocity.auth.AuthenticationListener
import dev.slne.surf.core.velocity.plugin
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ConnectionListener {
    @Subscribe(priority = Short.MIN_VALUE)
    fun onLogin(event: PlayerChooseInitialServerEvent) {
        val newServer = event.initialServer.getOrNull()?.serverInfo?.name
            ?: error("Player has no initial server")
        val player = event.player

        plugin.pluginContainer.launch {
            handleConnect(
                player.uniqueId,
                player.username,
                player.remoteAddress.toString(),
                player.remoteAddress.address,
                newServer,
                event.player.gameProfile

            )
        }
    }

    @Subscribe
    fun onConnected(event: ServerConnectedEvent) {
        handleSwitch(
            event.player.uniqueId,
            event.player.username,
            event.previousServer.getOrNull()?.serverInfo?.name ?: return,
            event.server.serverInfo.name
        )
    }

    @Subscribe
    suspend fun onDisconnect(event: DisconnectEvent) {
        handleDisconnect(
            event.player.uniqueId,
            event.player.username,
            event.loginStatus
        )
    }

    private suspend fun handleConnect(
        playerUuid: UUID,
        playerName: String,
        remoteAddress: String,
        inetAddress: InetAddress,
        initialServer: String,
        gameProfile: GameProfile
    ) {
        println("[new connection] $playerName ($remoteAddress) connected to '$initialServer'")

        val player = SurfPlayerService.getOrLoadOrCreatePlayerByUuid(
            playerUuid
        ).apply {
            if (firstSeen == null) {
                firstSeen = OffsetDateTime.now()
            }

            lastSeen = OffsetDateTime.now()
            lastKnownName = playerName
            currentServer = SurfServerService.getServerByName(initialServer)
            currentProxy = SurfProxyServer.current()
            lastKnownIpAddress = inetAddress
            transferred = AuthenticationListener.transfers.remove(playerUuid)
        }

        SurfPlayerService.cachePlayer(player)

        surfEventBus.fire(
            SurfPlayerConnectEvent(
                player
            )
        )

        val playerTexture = gameProfile.properties.find { it.name == "textures" }?.value
        val playerSignature = gameProfile.properties.find { it.name == "textures" }?.signature

        SurfPlayerService.savePlayer(player)

        SurfPlayerIpAddressHistoryService.handleNewIpAddress(player)
        SurfPlayerNameHistoryService.handleNewName(player)


        if (playerTexture != null && playerSignature != null) {
            SurfPlayerTextureHistoryService.handleNewTexture(player, playerTexture, playerSignature)
        }
    }

    private fun handleSwitch(
        playerUuid: UUID,
        playerName: String,
        fromServer: String,
        toServer: String
    ) {
        println("[connection update] $playerName was redirected from '$fromServer' to '$toServer'")


        val server = SurfServer[toServer] ?: error("SurfServer '$toServer' not found")
        val player = SurfPlayerService.players.firstOrNull { it.uuid == playerUuid }
            ?.copy(currentServer = server)
            ?: error("Player $playerName is not cached")

        SurfPlayerService.cachePlayer(player)
    }

    private suspend fun handleDisconnect(
        playerUuid: UUID,
        playerName: String,
        successfullyLogin: DisconnectEvent.LoginStatus
    ) {
        if (successfullyLogin == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            println("[connection closed] $playerName disconnected")
        } else {
            println("[connection closed] $playerName tried to connect: ${successfullyLogin.name}")
        }


        val player = SurfPlayerService.findPlayerByUuid(playerUuid) ?: return

        surfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        SurfPlayerService.invalidatePlayer(player.uuid)

        SurfPlayerService.savePlayer(player.apply {
            lastSeen = OffsetDateTime.now()
        })
    }
}