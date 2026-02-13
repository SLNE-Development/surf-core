package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.PaperBootstrap
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun surfCoreCommand() = commandTree("core") {
    withPermission(PermissionRegistry.COMMAND_CORE)
    literalArgument("reload") {
        anyExecutor { executor, _ ->
            PaperBootstrap.surfServerConfigHolder.reload()

            executor.sendText {
                appendSuccessPrefix()
                success("Die Konfiguration wurde neu geladen.")
            }
        }
    }

    literalArgument("clearinternalplayercache") {
        anyExecutor { executor, _ ->
            surfPlayerService.clearPlayers()

            executor.sendText {
                appendSuccessPrefix()
                success("Die Spieler-Caches wurden geleert.")
            }
        }
    }

    literalArgument("testawaitingsend") {
        surfBackendServerArgument("backend") {
            playerExecutor { player, args ->
                val surfPlayer = player.surfPlayer
                val backend: SurfServer by args

                player.sendText {
                    appendInfoPrefix()
                    info("Du wirst auf den Server gesendet...")
                }

                plugin.launch {
                    val result = surfCoreApi.sendPlayerAwaiting(surfPlayer, backend)

                    if (result.isSuccessful()) {
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du wurdest erfolgreich zum Server ")
                            variableValue(backend.name)
                            success(" gesendet.")
                        }
                    } else {
                        player.sendText {
                            appendErrorPrefix()
                            error("Es gab ein Problem beim Senden zum Server: ${result.status.name}")
                        }
                    }
                }
            }
        }
    }
}