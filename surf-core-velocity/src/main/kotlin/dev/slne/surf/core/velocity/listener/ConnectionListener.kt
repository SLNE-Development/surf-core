package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.util.GameProfile
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.history.surfPlayerIpAddressHistoryService
import dev.slne.surf.core.core.common.player.history.surfPlayerNameHistoryService
import dev.slne.surf.core.core.common.player.history.surfPlayerTextureHistoryService
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.server.surfServerService
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

    @Subscribe(priority = Short.MIN_VALUE)
    fun onConnected(event: ServerPreConnectEvent) {
        handleSwitch(
            event.player.uniqueId,
            event.player.username,
            event.previousServer?.serverInfo?.name ?: return,
            event.result.server.getOrNull()?.serverInfo?.name ?: return
        )
    }

    @Subscribe
    suspend fun onDisconnect(event: DisconnectEvent) {
        handleDisconnect(
            event.player.uniqueId,
            event.player.username,
            event.loginStatus == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN
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

        val player = surfPlayerService.getOrLoadOrCreatePlayerByUuid(
            playerUuid
        ).apply {
            if (firstSeen == null) {
                firstSeen = OffsetDateTime.now()
            }

            lastSeen = OffsetDateTime.now()
            lastKnownName = playerName
            currentServer = surfServerService.getServerByName(initialServer)
            currentProxy = SurfServer.current()
            lastKnownIpAddress = inetAddress
        }

        surfPlayerService.cachePlayer(player)

        surfEventBus.fire(
            SurfPlayerConnectEvent(
                player
            )
        )

        val playerTexture = gameProfile.properties.find { it.name == "textures" }?.value
        val playerSignature = gameProfile.properties.find { it.name == "textures" }?.signature

        surfPlayerService.savePlayer(player)

        surfPlayerIpAddressHistoryService.handleNewIpAddress(player)
        surfPlayerNameHistoryService.handleNewName(player)


        if (playerTexture != null && playerSignature != null) {
            surfPlayerTextureHistoryService.handleNewTexture(
                player,
                playerTexture,
                playerSignature
            )
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
        val player =
            surfPlayerService.players.firstOrNull { it.uuid == playerUuid }
                ?.copy(currentServer = server)
                ?: error("Player $playerName is not cached")

        surfPlayerService.cachePlayer(player)
    }

    private suspend fun handleDisconnect(
        playerUuid: UUID,
        playerName: String,
        successfullyLogin: Boolean
    ) {
        if (successfullyLogin) {
            println("[connection closed] $playerName disconnected")
        } else {
            println("[connection closed] $playerName tried to connect")
        }


        val player = surfPlayerService.findPlayerByUuid(playerUuid) ?: return

        surfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        surfPlayerService.invalidatePlayer(player.uuid)

        surfPlayerService.savePlayer(player.apply {
            lastSeen = OffsetDateTime.now()
        })
    }
}