package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.scope
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.logger
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
import dev.slne.surf.core.velocity.player.VelocityPlayerSessionRegistry
import dev.slne.surf.core.velocity.plugin
import kotlinx.coroutines.withTimeoutOrNull
import java.time.OffsetDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

object ConnectionListener {
    private val log = logger()

    @Subscribe(priority = Short.MIN_VALUE)
    suspend fun onLogin(event: LoginEvent) {
        withTimeoutOrNull(5.seconds) {
            handleConnect(event.player)
        } ?: run {
            val session = VelocityPlayerSessionRegistry.remove(event.player)

            if (session != null) {
                withTimeoutOrNull(1.seconds) {
                    SurfPlayerService.invalidatePlayerIfEqualsAndAwait(session)
                }
            }

            log.atWarning()
                .log(
                    "[connection timeout] %s (%s) took too long to log in",
                    event.player.username,
                    event.player.remoteAddress,
                )

            event.result = ResultedEvent.ComponentResult.denied(
                failedToLoadDataComponent(
                    "Internal server error: Timeout while loading player data.",
                    SurfPlayerErrorService.saveError(
                        playerUuid = event.player.uniqueId,
                        staffMessage = "Player timed out while logging in, most likely a internal database issue. Is rabbit or microservice down?",
                        scope = plugin.pluginContainer.scope,
                    )
                )
            )
        }
    }

    @Subscribe(priority = Short.MIN_VALUE)
    suspend fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val serverName = event.initialServer.getOrNull()
            ?.serverInfo
            ?.name
            ?: error("Player has no initial server")

        handleInitialServer(event.player, serverName)
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
    suspend fun onConnected(event: ServerConnectedEvent) {
        val previousServer = event.previousServer.getOrNull()
            ?.serverInfo
            ?.name
            ?: return

        handleSwitch(
            player = event.player,
            fromServer = previousServer,
            toServer = event.server.serverInfo.name,
        )
    }

    @Subscribe
    suspend fun onDisconnect(event: DisconnectEvent) {
        handleDisconnect(
            event.player,
            event.loginStatus,
        )
    }

    private suspend fun handleInitialServer(player: Player, serverName: String) {
        log.atInfo()
            .log(
                "[New connection] %s (%s) connected to '%s'",
                player.username,
                player.remoteAddress,
                serverName
            )

        val current = VelocityPlayerSessionRegistry[player]
        if (current == null) {
            disconnectMissingPlayerSession(player)
            return
        }

        val updated = current.copy(
            currentServerName = serverName,
        )

        if (!SurfPlayerService.replacePlayerIfEqualsAndAwait(current, updated)) {
            disconnectMissingPlayerSession(player)
            return
        }

        VelocityPlayerSessionRegistry.replace(
            player,
            current,
            updated,
        )
    }

    private fun disconnectMissingPlayerSession(player: Player) {
        player.disconnect(
            failedToLoadDataComponent(
                "Internal server error: Failed to receive data while connecting to the initial server.",
                SurfPlayerErrorService.saveError(
                    playerUuid = player.uniqueId,
                    staffMessage = "Player connection session was no longer authoritative while connecting to the initial server.",
                    scope = plugin.pluginContainer.scope,
                ),
            )
        )
    }

    private suspend fun handleConnect(player: Player) {
        val playerUuid = player.uniqueId
        val cached = SurfPlayerService.getOrLoadOrCreatePlayerByUuid(playerUuid)

        val surfPlayer = cached.copy(
            lastKnownName = player.username,
            firstSeen = cached.firstSeen ?: OffsetDateTime.now(),
            lastSeen = OffsetDateTime.now(),
            currentServerName = player.currentServer.getOrNull()?.serverInfo?.name
                ?: cached.currentServerName,
            currentProxyName = SurfProxyServer.current().name,
            lastKnownIpAddress = player.remoteAddress.address,
            transferred = AuthenticationListener.transfers.remove(playerUuid),
            connectionSessionId = UUID.randomUUID(),
        )

        SurfPlayerService.cachePlayerAndAwait(surfPlayer)
        VelocityPlayerSessionRegistry.register(player, surfPlayer)

        SurfEventBus.fire(SurfPlayerConnectEvent(surfPlayer))

        val textureProperty = player.gameProfile.properties.find { it.name == "textures" }
        val playerTexture = textureProperty?.value
        val playerSignature = textureProperty?.signature

        SurfPlayerService.savePlayer(surfPlayer)

        SurfPlayerIpAddressHistoryService.handleNewIpAddress(surfPlayer)
        SurfPlayerNameHistoryService.handleNewName(surfPlayer)


        if (playerTexture != null && playerSignature != null) {
            SurfPlayerTextureHistoryService.handleNewTexture(
                surfPlayer,
                playerTexture,
                playerSignature
            )
        }
    }

    private suspend fun handleSwitch(
        player: Player,
        fromServer: String,
        toServer: String,
    ) {
        log.atInfo()
            .log(
                "[Connection update] %s was redirected from '%s' to '%s'",
                player.username,
                fromServer,
                toServer
            )

        val current = VelocityPlayerSessionRegistry[player] ?: return

        val updated = current.copy(
            currentServerName = toServer,
        )

        if (!SurfPlayerService.replacePlayerIfEqualsAndAwait(current, updated)) {
            log.atWarning()
                .log(
                    "Ignoring server switch for stale player session %s (%s)",
                    player.username,
                    player.uniqueId,
                )
            return
        }

        VelocityPlayerSessionRegistry.replace(
            player,
            current,
            updated,
        )
    }

    private suspend fun handleDisconnect(
        velocityPlayer: Player,
        successfullyLogin: DisconnectEvent.LoginStatus,
    ) {
        if (successfullyLogin == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            log.atInfo()
                .log(
                    "[Connection closed] %s (%s) disconnected",
                    velocityPlayer.username,
                    velocityPlayer.remoteAddress
                )
        } else {
            log.atInfo()
                .log(
                    "[Connection closed] %s failed to connect: %s",
                    velocityPlayer.username,
                    successfullyLogin.name
                )
        }

        val player = VelocityPlayerSessionRegistry.remove(velocityPlayer) ?: return

        SurfEventBus.fire(SurfPlayerDisconnectEvent(player))

        val removed = SurfPlayerService.invalidatePlayerIfEqualsAndAwait(player)

        if (!removed) {
            log.atInfo()
                .log(
                    "Did not invalidate stale player session %s for %s because Redis already contains a newer state",
                    player.connectionSessionId,
                    player.uuid,
                )
        }

        SurfPlayerService.savePlayer(
            player.copy(
                lastSeen = OffsetDateTime.now(),
            )
        )
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
            issue = true
        )

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
            issue = true
        )
}