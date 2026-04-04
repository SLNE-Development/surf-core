package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun hubCommand() = commandTree("hub") {
    withAliases("lobby", "l")
    withPermission(PermissionRegistry.COMMAND_HUB)

    playerExecutor { player, _ ->
        player.sendText {
            appendCorePrefix()
            info("Du wirst zum Hub gesendet...")
        }

        val server = SurfCoreApi.getServerWithLeastPlayers("lobby")

        if (server == null) {
            player.sendText {
                appendCorePrefix()
                error("Es konnte kein Hub-Server gefunden werden.")
            }
            return@playerExecutor
        }

        plugin.launch {
            val result = SurfCoreApi.sendPlayerAwaiting(player.surfPlayer, server)

            if (result.isSuccessful()) {
                player.sendText {
                    appendCorePrefix()
                    success("Du wurdest erfolgreich zum Hub gesendet.")
                }
            } else {
                player.sendText {
                    appendCorePrefix()
                    error("Es gab ein Problem beim Senden zum Hub")

                    result.velocityMessage.let {
                        if (it != null) {
                            error(": ")
                            append(it)
                        } else {
                            error(".")
                        }
                    }
                }
            }
        }
    }
}