package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPreShutdownEvent
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.api.velocity.util.surfPlayer
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import net.kyori.adventure.text.format.TextDecoration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UnstableApiUsage")
object VelocityServerListener {

    @Subscribe(priority = Short.MIN_VALUE)
    fun onPreShutdown(event: ProxyPreShutdownEvent, continuation: Continuation) {
        val currentProxy = SurfProxyServer.current()

        surfCoreApi.getOnlinePlayers().forEach {
            it.sendText {
                appendInfoPrefix()
                error("SYSTEM-NEUSTART", TextDecoration.BOLD)
                spacer(":")
                spacer("Derzeit werden Hintergrundsysteme neugestartet. Bitte habt Verständnis, sollten in diesem Zeitraum Probleme auftreten!")
            }
        }

        val targetProxies = surfServerService.proxyServers
            .filter { it.name != currentProxy.name }
            .sortedBy { it.getPlayerCount() }

        val successCount = AtomicInteger(0)
        val failureMap =
            ConcurrentHashMap<SurfProxyServerConnectionResult.Status, ObjectSet<String>>()

        plugin.pluginContainer.launch {
            try {
                plugin.logger.info("Proxy shutting down, moving players to other proxies...")

                supervisorScope {
                    val players = plugin.proxy.allPlayers

                    val jobs = players.map { player ->
                        async {
                            val target =
                                targetProxies.firstOrNull { it.getPlayerCount() < it.maxPlayers }

                            if (target == null) {
                                failureMap.computeIfAbsent(
                                    SurfProxyServerConnectionResult.Status.SERVER_NOT_FOUND
                                ) {
                                    mutableObjectSetOf()
                                }.add(player.username)

                                player.disconnect(buildDisconnectComponent())
                                return@async
                            }

                            try {
                                val result =
                                    surfCoreApi.sendPlayerAwaiting(player.surfPlayer, target)

                                if (result.status == SurfProxyServerConnectionResult.Status.SUCCESS) {
                                    successCount.incrementAndGet()
                                } else {
                                    failureMap.computeIfAbsent(result.status) {
                                        mutableObjectSetOf()
                                    }.add(player.username)

                                    player.disconnect(buildDisconnectComponent())
                                }
                            } catch (e: Exception) {
                                failureMap.computeIfAbsent(
                                    SurfProxyServerConnectionResult.Status.ERR_UNKNOWN
                                ) {
                                    mutableObjectSetOf()
                                }.add(player.username)

                                plugin.logger.error(
                                    "Failed to move player ${player.username} during shutdown!",
                                    e
                                )
                            }
                        }
                    }

                    plugin.logger.info("Redirecting ${jobs.size} players to other proxies...")
                    jobs.awaitAll()
                }

                val totalFailures = failureMap.values.sumOf { it.size }
                val totalPlayers = successCount.get() + totalFailures

                plugin.logger.info("*" + "-".repeat(20) + "Shutdown Report " + "-".repeat(20) + "*")
                plugin.logger.info("Total players processed: $totalPlayers")
                plugin.logger.info("Successfully redirected: ${successCount.get()}")
                plugin.logger.info("Failed: $totalFailures")

                failureMap.forEach { (status, players) ->
                    plugin.logger.warn(
                        "Status: $status | Count: ${players.size} | Players: ${
                            players.joinToString(", ")
                        }"
                    )
                }

                plugin.logger.info("*" + "-".repeat(20) + "End Shutdown Report " + "-".repeat(20) + "*")
                plugin.logger.info("All redirect attempts completed. Proxy can safely shut down now.")

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
