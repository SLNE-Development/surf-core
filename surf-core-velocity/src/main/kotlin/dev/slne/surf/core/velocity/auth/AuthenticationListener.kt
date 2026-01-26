@file:Suppress("UnstableApiUsage")

package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.connection.PreTransferEvent
import com.velocitypowered.api.event.player.CookieReceiveEvent
import com.velocitypowered.api.network.HandshakeIntent
import com.velocitypowered.api.network.ProtocolState
import dev.slne.surf.surfapi.core.api.util.random

object AuthenticationListener {
    @Subscribe
    fun onTransfer(event: PreLoginEvent) {
        if (event.connection.protocolState != ProtocolState.HANDSHAKE) {
            return
        }

        if (event.connection.handshakeIntent != HandshakeIntent.TRANSFER) {
            return
        }

        println("[pre-transfer] ${event.username} is transferring to the proxy, but cannot request cookies yet.")
    }

    @Subscribe
    fun onCookieReceive(event: CookieReceiveEvent) {
        if (event.originalKey != authentificationService.key) {
            return
        }

        event.originalData?.let {
            authentificationService.authenticate(event.player.uniqueId, it)
        }
    }

    @Subscribe
    fun onPreTransfer(event: PreTransferEvent) {
        val player = event.player()
        val token = generateToken()
        authentificationService.preTransfer(player.uniqueId, token)

        player.storeCookie(authentificationService.key, token)
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}