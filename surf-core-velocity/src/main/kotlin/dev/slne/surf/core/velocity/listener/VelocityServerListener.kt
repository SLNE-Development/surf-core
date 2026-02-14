package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPreShutdownEvent
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.velocity.util.surfPlayer
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

@Suppress("UnstableApiUsage")
object VelocityServerListener {
    @Subscribe(priority = Short.MIN_VALUE)
    fun onPreShutdown(event: ProxyPreShutdownEvent, continuation: Continuation) {
        val currentProxy = SurfProxyServer.current()

        val targetProxies = surfServerService.servers
            .filter { it.isProxy() && it.name != currentProxy.name }
            .sortedBy { it.getPlayerCount() }

        plugin.pluginContainer.launch {

            try {
                plugin.logger.info("Proxy shutting down, moving players to other proxies...")
                supervisorScope {
                    val jobs = plugin.proxy.allPlayers.map { player ->
                        async {

                            val target =
                                targetProxies.firstOrNull { it.getPlayerCount() < it.maxPlayers }

                            if (target == null) {
                                player.disconnect(buildDisconnectComponent())
                                return@async
                            }

                            try {
                                surfCoreApi.sendPlayerAwaiting(player.surfPlayer)
                                target.pullPlayers(player.surfPlayer)
                            } catch (e: Exception) {
                                plugin.logger.error(
                                    "Failed to move player ${player.username} to another proxy during shutdown!",
                                    e
                                )
                            }
                        }
                    }

                    plugin.logger.info("Redirecting ${jobs.size} players to other proxies...")

                    jobs.awaitAll()
                }

                plugin.logger.info("All players have been moved, proxy can safely shut down now.")

            } catch (e: Exception) {
                plugin.logger.error(
                    "Unexpected error during proxy shutdown handling!",
                    e
                )
            } finally {
                continuation.resume()
            }
        }
    }

    private fun buildDisconnectComponent() = buildText {
        appendNewline(2)
        primary("CASTCRAFTER")
        appendNewline()
        primary("COMMUNITY SERVER")
        appendNewline(2)
        error("DER PROXY WIRD HERUNTERGEFAHREN.")
        appendNewline(3)
        spacer("Es wurde kein anderer Proxy Server gefunden, zu dem du wechseln könntest.")
        appendNewline(2)
        primary("discord.gg/castcrafter")
    }
}