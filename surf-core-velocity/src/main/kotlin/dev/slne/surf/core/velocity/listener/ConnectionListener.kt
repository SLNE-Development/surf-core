package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.github.shynixn.mccoroutine.velocity.scope
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.util.GameProfile
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.player.history.SurfPlayerIpAddressHistoryService
import dev.slne.surf.core.core.common.player.history.SurfPlayerNameHistoryService
import dev.slne.surf.core.core.common.player.history.SurfPlayerTextureHistoryService
import dev.slne.surf.core.core.common.util.niceRed
import dev.slne.surf.core.velocity.auth.AuthenticationListener
import dev.slne.surf.core.velocity.plugin
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

object ConnectionListener {
    @Subscribe(priority = Short.MIN_VALUE)
    suspend fun onLogin(event: LoginEvent) {
        withTimeoutOrNull(5.seconds) {
            handleConnect(
                playerUuid = event.player.uniqueId,
                playerName = event.player.username,
                inetAddress = event.player.remoteAddress.address,
                initialServer = event.player.currentServer.getOrNull()?.serverInfo?.name,
                gameProfile = event.player.gameProfile
            )
        } ?: {
            println("[connection timeout] ${event.player.username} (${event.player.remoteAddress}) took too long to log in")

            event.result =
                ResultedEvent.ComponentResult.denied(
                    failedToLoadDataComponent(
                        "Internal server error: Timeout while loading player data.",
                        SurfPlayerErrorService.saveError(
                            playerUuid = event.player.uniqueId,
                            staffMessage = "Player timed out while logging in, most likely a internal database issue. Is rabbit or microservice down?",
                            scope = plugin.pluginContainer.scope
                        )
                    )
                )
        }
    }

    @Subscribe(priority = Short.MIN_VALUE)
    fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val newServer = event.initialServer.getOrNull()?.serverInfo?.name
            ?: error("Player has no initial server")
        val player = event.player

        plugin.pluginContainer.launch {
            handleInitialServer(
                player,
                newServer
            )
        }
    }

    @Subscribe(priority = Short.MIN_VALUE)
    fun onKickedFromServer(event: KickedFromServerEvent) {
        if (!event.kickedDuringServerConnect()) {
            return
        }

        if (event.serverKickReason.isPresent) {
            return
        }

        if (event.result !is KickedFromServerEvent.DisconnectPlayer) {
            return
        }

        event.result = KickedFromServerEvent.DisconnectPlayer.create(
            failedToConnectComponent(
                "Internal server error: Failed to connect to the target server.",
                SurfPlayerErrorService.saveError(
                    playerUuid = event.player.uniqueId,
                    staffMessage = "Player was kicked from target server during connection to ${event.server.serverInfo.name}, no other information available.",
                    scope = plugin.pluginContainer.scope
                )
            )
        )
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

    private fun handleInitialServer(player: Player, serverName: String) {
        println("[new connection] ${player.username} (${player.remoteAddress}) connected to '$serverName'")

        val surfPlayer = SurfPlayerService.findPlayerByUuid(player.uniqueId)

        if (surfPlayer == null) {
            player.disconnect(
                failedToLoadDataComponent(
                    "Internal server error: Failed to receive data while connecting to the initial server.",
                    SurfPlayerErrorService.saveError(
                        playerUuid = player.uniqueId,
                        staffMessage = "Player could not connect to initial server, most likely a redis issue. Is redis down?",
                        scope = plugin.pluginContainer.scope
                    )
                )
            )
            return
        }

        SurfPlayerService.cachePlayer(
            surfPlayer.copy(
                currentServerName = serverName
            )
        )
    }

    private suspend fun handleConnect(
        playerUuid: UUID,
        playerName: String,
        inetAddress: InetAddress,
        initialServer: String?,
        gameProfile: GameProfile
    ) {
        val player = SurfPlayerService.getOrLoadOrCreatePlayerByUuid(
            playerUuid
        ).apply {
            if (firstSeen == null) {
                firstSeen = OffsetDateTime.now()
            }

            lastSeen = OffsetDateTime.now()
            lastKnownName = playerName
            if (initialServer != null) {
                currentServerName = initialServer
            }
            currentProxyName = SurfProxyServer.current().name
            lastKnownIpAddress = inetAddress
            transferred = AuthenticationListener.transfers.remove(playerUuid)
        }

        SurfPlayerService.cachePlayer(player)

        SurfEventBus.fire(
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

        val player = SurfPlayerService.players.firstOrNull { it.uuid == playerUuid }
            ?.copy(currentServerName = toServer)
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
            println("[connection closed] $playerName failed to connect: ${successfullyLogin.name}")
        }


        val player = SurfPlayerService.findPlayerByUuid(playerUuid) ?: return

        SurfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        SurfPlayerService.invalidatePlayer(player.uuid)

        SurfPlayerService.savePlayer(player.apply {
            lastSeen = OffsetDateTime.now()
        })
    }


    private fun failedToLoadDataComponent(message: String, errorCode: String) =
        CommonComponents.renderDisconnectMessage(
            SurfComponentBuilder(),
            "DEINE SPIELERDATEN KONNTEN NICHT GELADEN WERDEN.",
            {
                spacer("Fehlercode: ")
                niceRed(errorCode)
                appendNewline()
                error(message)
                appendNewline(3)
                spacer("Beim Laden deiner Spielerdaten ist ein interner Fehler aufgetreten.")
            },
            {
                spacer("Sollte das Problem weiterhin bestehen, wende dich bitte an den Support.")
                appendNewline(2)
                primary("discord.gg/castcrafter")
            })

    private fun failedToConnectComponent(message: String, errorCode: String) =
        CommonComponents.renderDisconnectMessage(
            SurfComponentBuilder(),
            "DEINE VERBINDUNG KONNTE NICHT HERGESTELLT WERDEN.",
            {
                spacer("Fehlercode: ")
                niceRed(errorCode)
                appendNewline()
                error(message)
                appendNewline(3)
                spacer("Beim Herstellen der Verbindung ist ein interner Fehler aufgetreten.")
            },
            {
                spacer("Sollte das Problem weiterhin bestehen, wende dich bitte an den Support.")
                appendNewline(2)
                primary("discord.gg/castcrafter")
            })
}