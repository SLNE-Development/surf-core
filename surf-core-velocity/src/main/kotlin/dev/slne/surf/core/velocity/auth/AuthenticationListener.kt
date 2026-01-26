@file:Suppress("UnstableApiUsage")

package dev.slne.surf.core.velocity.auth

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.connection.PreTransferEvent
import com.velocitypowered.api.event.player.CookieReceiveEvent
import com.velocitypowered.api.network.HandshakeIntent
import dev.slne.surf.surfapi.core.api.util.random
import kotlin.jvm.optionals.getOrNull

object AuthenticationListener {
    @Subscribe
    fun onTransfer(event: PreLoginEvent) {
        if (event.connection.handshakeIntent != HandshakeIntent.TRANSFER) {
            return
        }

        println("[pre-transfer] ${event.username} is transferring to the proxy, but cannot request cookies yet.")
    }

    @Subscribe
    fun onLogin(event: LoginEvent, continuation: Continuation) {
        println("[login] ${event.player.username} is logging in with protocol state ${event.player.protocolState}.")

        if (event.player.handshakeIntent != HandshakeIntent.TRANSFER) {
            println("[login] ${event.player.username} is not transferring to the proxy, no cookie needed.")
            continuation.resume()
            return
        }

        println("[login] ${event.player.username} is transferring to the proxy, requesting cookie for authentication.")

        authentificationService.continuations[event.player.uniqueId] = continuation
        event.player.requestCookie(authentificationService.key)
        event.player.requestCookie(authentificationService.lastServerKey)

        println("[login] Cookie request sent to ${event.player.username}.")
    }

    @Subscribe
    fun onCookieReceive(event: CookieReceiveEvent) {
        when (event.originalKey) {
            authentificationService.lastServerKey -> {
                event.originalData?.let {
                    val lastServerName = String(it)
                    println("[cookie-receive] Received last server cookie for ${event.player.username}: $lastServerName")
                    authentificationService.lastServers[event.player.uniqueId] = lastServerName
                }
            }

            authentificationService.key -> {
                println("[cookie-receive] Received auth cookie for ${event.player.username}, authenticating.")

                event.originalData?.let {
                    authentificationService.authenticate(event.player.uniqueId, it)
                }
            }

            else -> {}
        }
    }

    @Subscribe
    fun onPreTransfer(event: PreTransferEvent) {
        val player = event.player()
        val token = generateToken()

        authentificationService.preTransfer(player.uniqueId, token)
        player.storeCookie(authentificationService.key, token)
        player.storeCookie(
            authentificationService.lastServerKey,
            player.currentServer.getOrNull()?.serverInfo?.name?.toByteArray()
        )
    }

    private fun generateToken(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }
}