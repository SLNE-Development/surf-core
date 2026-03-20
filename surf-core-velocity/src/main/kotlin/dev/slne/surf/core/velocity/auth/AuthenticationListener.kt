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
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.velocityCoreConfigManager
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.random
import java.util.*
import kotlin.jvm.optionals.getOrNull

object AuthenticationListener {
    val transfers = mutableObjectSetOf<UUID>()

    @Subscribe
    fun onLogin(event: LoginEvent, continuation: Continuation) {
        if (event.player.handshakeIntent != HandshakeIntent.TRANSFER) {
            if (event.player.hasPermission("surf.core.bypass")) {
                continuation.resume()
                event.player.sendText {
                    appendWarningPrefix()
                    error("Du verbindest dich über eine inoffizielle Methode, hast aber die Berechtigung zum Umgehen.")
                }
                return
            }

            event.player.virtualHost.getOrNull()?.hostString?.let { domain ->
                if (velocityCoreConfigManager.config.blockedDomains.any {
                        it.equals(
                            domain,
                            true
                        )
                    }) {
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
            }

            continuation.resume()
            return
        }

        transfers.add(event.player.uniqueId)

        authenticationService.continuations[event.player.uniqueId] = continuation
        event.player.requestCookie(authenticationService.key)
    }

    @Subscribe
    fun onCookieReceive(event: CookieReceiveEvent) {
        if (event.originalKey != authenticationService.key) return

        event.originalData?.let {
            authenticationService.authenticate(event.player.uniqueId, it)
        }
    }

    @Subscribe
    fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val player = event.player
        val lastServerName = authenticationService.lastServerMap.remove(player.uniqueId) ?: return

        if (event.player.handshakeIntent != HandshakeIntent.TRANSFER) {
            return
        }

        plugin.proxy.getServer(lastServerName).getOrNull()?.let {
            event.setInitialServer(it)
        }
    }

    @Subscribe
    fun onPreTransfer(event: PreTransferEvent) {
        val player = event.player()
        val token = generateToken()

        authenticationService.preTransfer(player.uniqueId, token)

        player.storeCookie(authenticationService.key, token)

        player.currentServer.getOrNull()?.serverInfo?.name?.let {
            authenticationService.lastServerMap[player.uniqueId] = it
        }
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}
