package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.kyori.adventure.audience.Audience

fun networkSendCommand() = commandTree("nsend") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SEND)

    literalArgument("player") {
        surfPlayerArgument("player") {
            surfServerArgument("server") {
                anyExecutor { executor, args ->
                    val player: SurfPlayer by args
                    val server: CommonSurfServer by args

                    when (val commonServer = server) {
                        is SurfProxyServer -> {
                            plugin.launch {
                                val result = SurfCoreApi.sendPlayerAwaiting(player, commonServer)

                                if (result.isSuccessful()) {
                                    executor.sendText {
                                        appendCorePrefix()
                                        success("Der Spieler ")
                                        variableValue(player.username)
                                        success(" wurde erfolgreich zum Proxy ")
                                        variableValue(commonServer.name)
                                        success(" gesendet.")
                                    }
                                } else {
                                    executor.sendText {
                                        appendCorePrefix()
                                        error("Der Spieler ")
                                        variableValue(player.username)
                                        error(" konnte nicht gesendet werden: ${result.status}")
                                    }
                                }
                            }
                        }

                        is SurfServer -> {
                            plugin.launch {
                                val result = SurfCoreApi.sendPlayerAwaiting(player, commonServer)

                                if (result.isSuccessful()) {
                                    executor.sendText {
                                        appendCorePrefix()
                                        success("Der Spieler ")
                                        variableValue(player.username)
                                        success(" wurde erfolgreich zum Server ")
                                        variableValue(commonServer.name)
                                        success(" gesendet.")
                                    }
                                } else {
                                    executor.sendText {
                                        appendCorePrefix()
                                        error("Der Spieler ")
                                        variableValue(player.username)
                                        error(" konnte nicht gesendet werden: ")

                                        result.velocityMessage?.let {
                                            append(it)
                                        } ?: error(result.status.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    literalArgument("server") {
        surfServerArgument("server") {
            surfServerArgument("targetServer") {
                anyExecutor { executor, args ->
                    val server: CommonSurfServer by args
                    val targetServer: CommonSurfServer by args
                    handleMultipleSend(executor, server.getPlayers(), targetServer, server.name)
                }
            }
        }
    }

    literalArgument("all") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args
                handleMultipleSend(
                    executor,
                    SurfCoreApi.getOnlinePlayers(),
                    targetServer,
                    "global"
                )
            }
        }
    }

    literalArgument("current") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val targetServer: CommonSurfServer by args
                val current = SurfServer.current()

                handleMultipleSend(
                    executor,
                    current.getPlayers(),
                    targetServer,
                    current.name
                )
            }
        }
    }
}

private fun handleMultipleSend(
    executor: Audience,
    players: ObjectSet<SurfPlayer>,
    target: CommonSurfServer,
    sourceName: String
) {
    if (players.isEmpty()) {
        executor.sendText {
            appendErrorPrefix()
            error("Es sind keine Spieler vorhanden.")
        }
        return
    }

    when (target) {
        is SurfProxyServer -> {
            plugin.launch {
                val results = coroutineScope {
                    players.map { player ->
                        async {
                            player to SurfCoreApi.sendPlayerAwaiting(player, target)
                        }
                    }.awaitAll()
                }

                val failed = results.filterNot { it.second.isSuccessful() }

                if (failed.isEmpty()) {
                    executor.sendText {
                        appendSuccessPrefix()
                        variableValue(results.size)
                        success(" Spieler wurden erfolgreich von ")
                        variableValue(sourceName)
                        success(" zu ")
                        variableValue(target.name)
                        success(" gesendet.")
                    }
                    return@launch
                }

                val grouped = failed.groupBy {
                    it.second.status.toString()
                }

                executor.sendText {
                    appendErrorPrefix()
                    error("Es konnten ")
                    variableValue(failed.size)
                    error(" von ")
                    variableValue(results.size)
                    error(" Spielern nicht gesendet werden:")

                    grouped.forEach { (reason, entries) ->
                        appendNewInfoPrefixedLine()
                        spacer(" - ")
                        error("$reason: ")

                        entries.forEachIndexed { index, entry ->
                            variableValue(entry.first.username)
                            if (index < entries.lastIndex) {
                                error(", ")
                            }
                        }
                    }
                }
            }
        }

        is SurfServer -> {
            plugin.launch {
                val results = coroutineScope {
                    players.map { player ->
                        async {
                            player to SurfCoreApi.sendPlayerAwaiting(player, target)
                        }
                    }.awaitAll()
                }

                val failed = results.filterNot { it.second.isSuccessful() }

                if (failed.isEmpty()) {
                    executor.sendText {
                        appendSuccessPrefix()
                        variableValue(results.size)
                        success(" Spieler wurden erfolgreich von ")
                        variableValue(sourceName)
                        success(" zu ")
                        variableValue(target.name)
                        success(" gesendet.")
                    }
                    return@launch
                }

                val grouped = failed.groupBy {
                    it.second.velocityMessage ?: it.second.status.toString()
                }

                executor.sendText {
                    appendErrorPrefix()
                    error("Es konnten ")
                    variableValue(failed.size)
                    error(" von ")
                    variableValue(results.size)
                    error(" Spielern nicht gesendet werden:")

                    grouped.forEach { (reason, entries) ->
                        appendNewInfoPrefixedLine()
                        spacer(" - ")
                        error("$reason: ")

                        entries.forEachIndexed { index, entry ->
                            variableValue(entry.first.username)
                            if (index < entries.lastIndex) {
                                error(", ")
                            }
                        }
                    }
                }
            }
        }
    }
}

