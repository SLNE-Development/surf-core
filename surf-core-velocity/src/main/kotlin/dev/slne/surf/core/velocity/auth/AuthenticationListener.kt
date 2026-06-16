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
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.random
import dev.slne.surf.core.velocity.permission.PermissionList
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.core.velocity.velocityCoreConfigManager
import java.util.*
import kotlin.jvm.optionals.getOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object AuthenticationListener {
    val transfers = mutableObjectSetOf<UUID>()

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
                event.result = ResultedEvent.ComponentResult.denied(
                    Component.text()
                        .append(Component.text("Hexoria Network").color(NamedTextColor.AQUA))
                        .appendNewline()
                        .append(
                            Component.text("INOFFIZIELLE DOMAIN")
                                .color(NamedTextColor.RED)
                                .decorate(TextDecoration.BOLD)
                        )
                        .appendNewline()
                        .append(
                            Component.text("Bitte verbinde dich über die offizielle Domain.")
                                .color(NamedTextColor.RED)
                        )
                        .appendNewline()
                        .append(Component.text("play.Hexoria.net").color(NamedTextColor.GOLD))
                        .build()
                )
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
    fun onCookieReceive(event: CookieReceiveEvent) {
        if (event.originalKey != AuthenticationService.key) return

        event.originalData?.let {
            AuthenticationService.authenticate(event.player.uniqueId, it)
        }
    }

    @Subscribe
    fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val player = event.player
        val lastServerName = AuthenticationService.lastServerMap.remove(player.uniqueId) ?: return

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

        AuthenticationService.preTransfer(player.uniqueId, token)

        player.storeCookie(AuthenticationService.key, token)
        player.currentServer.getOrNull()?.serverInfo?.name?.let {
            AuthenticationService.lastServerMap[player.uniqueId] = it
        }
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}