package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.paper.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.paper.command.argument.surfServerArgument
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.audience.Audience

fun networkSendCommand() = commandTree("nsend") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SEND)

    literalArgument("player") {
        surfPlayerArgument("player") {
            surfServerArgument("server") {
                anyExecutor { executor, args ->
                    val player: SurfPlayer by args
                    val target: CommonSurfServer by args

                    when (val commonServer = target) {
                        is SurfProxyServer -> {
                            player.send(commonServer)

                            executor.sendText {
                                appendSuccessPrefix()
                                success("Der Spieler ")
                                variableValue(player.username)
                                success(" wurde zum Proxy ")
                                variableValue(commonServer.name)
                                success(" gesendet.")
                            }
                        }

                        is SurfServer -> {
                            plugin.launch {
                                val result = surfCoreApi.sendPlayerAwaiting(player, commonServer)

                                if (result.isSuccessful()) {
                                    executor.sendText {
                                        appendSuccessPrefix()
                                        success("Der Spieler ")
                                        variableValue(player.username)
                                        success(" wurde erfolgreich zum Server ")
                                        variableValue(commonServer.name)
                                        success(" gesendet.")
                                    }
                                } else {
                                    executor.sendText {
                                        appendErrorPrefix()
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
                    val source: CommonSurfServer by args
                    val target: CommonSurfServer by args
                    handleMultipleSend(executor, source.getPlayers(), target, source.name)
                }
            }
        }
    }

    literalArgument("all") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val target: CommonSurfServer by args
                handleMultipleSend(
                    executor,
                    surfCoreApi.getOnlinePlayers(),
                    target,
                    "global"
                )
            }
        }
    }

    literalArgument("current") {
        surfServerArgument("targetServer") {
            anyExecutor { executor, args ->
                val target: CommonSurfServer by args
                val current = SurfServer.current()

                handleMultipleSend(
                    executor,
                    current.getPlayers(),
                    target,
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
            players.forEach { it.send(target) }

            executor.sendText {
                appendSuccessPrefix()
                variableValue(players.size)
                success(" Spieler wurden zum Proxy ")
                variableValue(target.name)
                success(" gesendet.")
            }
        }

        is SurfServer -> {
            plugin.launch {
                val results = players.map { player ->
                    player to surfCoreApi.sendPlayerAwaiting(player, target)
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
