package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.history.surfPlayerIpAddressHistoryService
import dev.slne.surf.core.core.common.player.history.surfPlayerNameHistoryService
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.surfServerConfig
import java.net.InetAddress
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
                newServer
            )
        }
    }

    @Subscribe
    fun onConnected(event: ServerPostConnectEvent) {
        handleSwitch(
            event.player.uniqueId,
            event.player.username,
            event.previousServer?.serverInfo?.name ?: return,
            event.player.currentServer.getOrNull()?.serverInfo?.name ?: return
        )
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        handleDisconnect(event.player.uniqueId, event.player.username)
    }

    private suspend fun handleConnect(
        playerUuid: UUID,
        playerName: String,
        remoteAddress: String,
        inetAddress: InetAddress,
        initialServer: String
    ) {
        println("[new connection] $playerName ($remoteAddress) connected to '$initialServer'")

        val player = surfPlayerService.getOrLoadOrCreatePlayerByUuid(
            playerUuid
        ).apply {
            if (firstSeen == null) {
                firstSeen = System.currentTimeMillis()
            }

            lastSeen = System.currentTimeMillis()
            lastKnownName = playerName
            currentServer = initialServer
            currentProxy = surfServerConfig.serverName
            lastKnownIpAddress = inetAddress
        }

        surfPlayerService.cachePlayer(player)

        surfEventBus.fire(
            SurfPlayerConnectEvent(
                player
            )
        )

        surfPlayerService.savePlayer(player)

        surfPlayerIpAddressHistoryService.handleNewIpAddress(player)
        surfPlayerNameHistoryService.handleNewName(player)
    }

    private fun handleSwitch(
        playerUuid: UUID,
        playerName: String,
        fromServer: String,
        toServer: String
    ) {
        println("[connection update] $playerName was redirected from '$fromServer' to '$toServer'")

        val player =
            surfPlayerService.players.find { it.uuid == playerUuid } ?: return
        player.currentServer = toServer

        surfPlayerService.cachePlayer(player)
    }

    private fun handleDisconnect(playerUuid: UUID, playerName: String) {
        println("[connection closed] $playerName disconnected")

        val player = surfPlayerService.findPlayerByUuid(playerUuid) ?: return

        surfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        surfPlayerService.invalidatePlayer(player.uuid)

        plugin.pluginContainer.launch {
            surfPlayerService.savePlayer(player.apply {
                lastSeen = System.currentTimeMillis()
            })
        }
    }
}