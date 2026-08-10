package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.util.appendCorePrefix
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

object NetworkSendCommandHandler {
    suspend fun sendPlayer(audience: Audience, player: SurfPlayer, target: CommonSurfServer) {
        val failure = connectionFailure(player, target)

        audience.sendText {
            appendCorePrefix()
            if (failure == null) {
                success("Der Spieler ")
                variableValue(player.username)
                success(" wurde erfolgreich zu ")
                variableValue(target.name)
                success(" gesendet.")
            } else {
                error("Der Spieler ")
                variableValue(player.username)
                error(" konnte nicht gesendet werden: ")
                append(failure)
            }
        }
    }

    suspend fun sendPlayers(
        audience: Audience,
        players: Collection<SurfPlayer>,
        target: CommonSurfServer,
        sourceName: String,
    ) {
        if (players.isEmpty()) {
            audience.sendText {
                appendCorePrefix()
                error("Es sind keine Spieler vorhanden.")
            }
            return
        }

        val results = coroutineScope {
            players.map { player ->
                async { player to connectionFailure(player, target) }
            }.awaitAll()
        }
        val failed = results.filter { it.second != null }
        if (failed.isEmpty()) {
            audience.sendText {
                appendCorePrefix()
                variableValue(results.size)
                success(" Spieler wurden erfolgreich von ")
                variableValue(sourceName)
                success(" zu ")
                variableValue(target.name)
                success(" gesendet.")
            }
            return
        }

        audience.sendText {
            appendCorePrefix()
            error("Es konnten ")
            variableValue(failed.size)
            error(" von ")
            variableValue(results.size)
            error(" Spielern nicht gesendet werden:")
            failed.groupBy { it.second }.forEach { (reason, entries) ->
                appendNewInfoPrefixedLine()
                spacer(" - ")
                reason?.let(::append)
                error(": ")
                entries.forEachIndexed { index, entry ->
                    variableValue(entry.first.username)
                    if (index < entries.lastIndex) error(", ")
                }
            }
        }
    }

    suspend fun sendSelf(audience: Audience, player: SurfPlayer, target: CommonSurfServer) {
        val targetType = if (target is SurfProxyServer) "Proxy" else "Server"
        audience.sendText {
            appendCorePrefix()
            info("Du wirst zum $targetType ")
            variableValue(target.name)
            info(" gesendet...")
        }
        val failure = connectionFailure(player, target)
        audience.sendText {
            appendCorePrefix()
            if (failure == null) {
                success("Du wurdest erfolgreich zum $targetType ")
                variableValue(target.name)
                success(" gesendet!")
            } else {
                error("Du konntest nicht zum $targetType verbunden werden: ")
                append(failure)
            }
        }
    }

    private suspend fun connectionFailure(
        player: SurfPlayer,
        target: CommonSurfServer,
    ): Component? = when (target) {
        is SurfProxyServer -> SurfCoreApi.sendPlayerAwaiting(player, target).let { result ->
            if (result.isSuccessful()) null else Component.text(result.status.toString())
        }

        is SurfServer -> SurfCoreApi.sendPlayerAwaiting(player, target).let { result ->
            if (result.isSuccessful()) null
            else result.velocityMessage ?: Component.text(result.status.toString())
        }
    }
}
