@file:Suppress("UnstableApiUsage")

package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreTransferEvent
import com.velocitypowered.api.event.player.CookieReceiveEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.network.HandshakeIntent
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.surfapi.core.api.util.random
import kotlin.jvm.optionals.getOrNull

object AuthenticationListener {
    @Subscribe
    fun onLogin(event: LoginEvent, continuation: Continuation) {
        if (event.player.handshakeIntent != HandshakeIntent.TRANSFER) {
            continuation.resume()
            return
        }

        authentificationService.continuations[event.player.uniqueId] = continuation
        event.player.requestCookie(authentificationService.key)
    }

    @Subscribe
    fun onCookieReceive(event: CookieReceiveEvent) {
        when (event.originalKey) {
            authentificationService.key -> {
                event.originalData?.let {
                    authentificationService.authenticate(event.player.uniqueId, it)
                }
            }

            else -> {}
        }
    }

    @Subscribe
    fun onInitialServer(event: PlayerChooseInitialServerEvent) {
        val player = event.player
        val lastServerName = authentificationService.lastServerMap.remove(player.uniqueId) ?: return

        plugin.proxy.getServer(lastServerName).getOrNull()?.let {
            event.setInitialServer(it)
        }
    }

    @Subscribe
    fun onPreTransfer(event: PreTransferEvent) {
        val player = event.player()
        val token = generateToken()

        authentificationService.preTransfer(player.uniqueId, token)

        player.storeCookie(authentificationService.key, token)

        player.currentServer.getOrNull()?.serverInfo?.name?.let {
            authentificationService.lastServerMap[player.uniqueId] = it
        }
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}