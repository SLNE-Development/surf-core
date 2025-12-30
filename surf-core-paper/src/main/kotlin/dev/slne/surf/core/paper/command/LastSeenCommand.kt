package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.core.core.common.util.formatDateMillis
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.Deferred

fun lastSeenCommand() = commandTree("lastseen") {
    withPermission(PermissionRegistry.COMMAND_LAST_SEEN)
    surfOfflinePlayerArgument("player") {
        anyExecutor { executor, args ->
            val player: Deferred<SurfPlayer?> by args

            plugin.launch {
                val surfPlayer = player.await() ?: run {
                    executor.sendText {
                        appendPrefix()
                        error("Der Spieler wurde nicht gefunden.")
                    }
                    return@launch
                }

                if (surfPlayer.isOnline()) {
                    executor.sendText {
                        appendPrefix()
                        success("Der Spieler ist aktuell online.")
                    }
                    return@launch
                }

                val lastSeen = surfPlayer.lastSeen ?: run {
                    executor.sendText {
                        appendPrefix()
                        error("Der Spieler wurde noch nie auf dem Netzwerk gesehen.")
                    }
                    return@launch
                }


                executor.sendText {
                    appendPrefix()
                    info("Der Spieler ")
                    variableValue(surfPlayer.lastKnownName ?: surfPlayer.uuid.toString())
                    info(" wurde zuletzt am ")
                    variableValue(lastSeen.formatDateMillis())
                    info(" um ")
                    variableValue(lastSeen.formatDateMillis())
                    info(" gesehen.")
                }
            }
        }
    }
}