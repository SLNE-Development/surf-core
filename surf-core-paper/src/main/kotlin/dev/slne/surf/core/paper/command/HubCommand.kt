package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
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

        val servers = SurfCoreApi.getServerByCategory("lobby")
            .sortedBy { it.getPlayerCount() }

        if (servers.isEmpty()) {
            player.sendText {
                appendCorePrefix()
                error("Es konnte kein passender Hub-Server gefunden werden.")
            }
            return@playerExecutor
        }

        plugin.launch {
            var success = false

            for (server in servers) {
                val result = SurfCoreApi.sendPlayerAwaiting(player.surfPlayer, server)
                if (result.isSuccessful()) {
                    success = true
                    break
                }
            }

            if (success) {
                player.sendText {
                    appendCorePrefix()
                    success("Du wurdest erfolgreich zum Hub gesendet.")
                }
            } else {
                player.sendText {
                    appendCorePrefix()
                    error("Es konnte kein passender Hub-Server gefunden werden.")
                }
            }
        }
    }
}
