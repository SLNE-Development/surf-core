package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun hubCommand() = commandTree("hub") {
    withAliases("lobby", "l")
    withPermission(PermissionRegistry.COMMAND_HUB)

    playerExecutor { player, _ ->
        player.sendText {
            appendInfoPrefix()
            info("Du wirst zum Hub gesendet...")
        }

        val server = surfCoreApi.getServerWithLeastPlayers("lobby")

        if (server == null) {
            player.sendText {
                appendErrorPrefix()
                error("Es konnte kein Hub-Server gefunden werden.")
            }
            return@playerExecutor
        }

        plugin.launch {
            val result = surfCoreApi.sendPlayerAwaiting(player.surfPlayer)

            if (result.isSuccessful()) {
                player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest erfolgreich zum Hub gesendet.")
                }
            } else {
                player.sendText {
                    appendErrorPrefix()
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