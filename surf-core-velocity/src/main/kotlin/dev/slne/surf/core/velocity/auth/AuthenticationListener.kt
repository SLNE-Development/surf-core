@file:Suppress("UnstableApiUsage")

package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreTransferEvent
import com.velocitypowered.api.event.player.CookieReceiveEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.network.HandshakeIntent
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.core.velocity.permission.PermissionList
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.velocityCoreConfigManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

object AuthenticationListener {
    val transfers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    @Subscribe
    fun onLogin(event: LoginEvent, continuation: Continuation) {
        if (event.player.handshakeIntent != HandshakeIntent.TRANSFER) {
            val domain = event.player.virtualHost.getOrNull()?.hostString
            val isTeamDomain = domain == velocityCoreConfigManager.config.teamDomain
            val isBlockedDomain =
                isTeamDomain || (domain != null && velocityCoreConfigManager.config.blockedDomains
                    .any { it.equals(domain, true) })

            if (isTeamDomain && event.player.hasPermission(PermissionList.TEAM_PERMISSION)) {
                continuation.resume()
                return
            }

            if (isBlockedDomain) {
                if (event.player.hasPermission(PermissionList.BYPASS_PERMISSION)) {
                    continuation.resume()
                    event.player.sendText {
                        appendWarningPrefix()
                        error("Du verbindest dich über eine inoffizielle Methode, hast aber die Berechtigung zum Umgehen.")
                    }
                    return
                }

                continuation.resume()
                event.result = ResultedEvent.ComponentResult.denied(buildText {
                    CommonComponents.renderDisconnectMessage(
                        this,
                        "INOFFIZIELLE DOMAIN",
                        {
                            error("Bitte verbinde dich über die offizielle Domain.")
                            appendNewline()
                            variableValue("castcrafter.de")
                        }
                    )
                })
                return
            }

            continuation.resume()
            return
        }

        transfers.add(event.player.uniqueId)

        AuthenticationService.continuations[event.player.uniqueId] = continuation
        event.player.requestCookie(AuthenticationService.key)
    }

    @Subscribe
    suspend fun onCookieReceive(event: CookieReceiveEvent) {
        if (event.originalKey != AuthenticationService.key) return

        event.originalData?.let {
            AuthenticationService.authenticate(event.player.uniqueId, it)
        }
    }

    @Subscribe
    suspend fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val player = event.player

        if (player.handshakeIntent != HandshakeIntent.TRANSFER) {
            return
        }

        val uuid = player.uniqueId
        val lastServerName = AuthenticationService.lastServerMap
            .getRemote(uuid)
            ?: return

        if (!AuthenticationService.lastServerMap.removeIfEqualsAndAwait(uuid, lastServerName)) {
            return
        }

        plugin.proxy.getServer(lastServerName)
            .ifPresent(event::setInitialServer)
    }

    @Subscribe
    suspend fun onPreTransfer(event: PreTransferEvent) {
        val player = event.player()
        val token = generateToken()

        AuthenticationService.preTransfer(player.uniqueId, token)

        player.currentServer
            .getOrNull()
            ?.serverInfo
            ?.name
            ?.let { serverName ->
                AuthenticationService.lastServerMap.putAndAwait(
                    player.uniqueId,
                    serverName,
                )
            }

        player.storeCookie(
            AuthenticationService.key,
            token,
        )
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}