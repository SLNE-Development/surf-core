package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.core.api.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import kotlinx.coroutines.Deferred
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun lastSeenCommand() = commandTree("lastseen") {
    withPermission(PermissionRegistry.COMMAND_LAST_SEEN)
    surfOfflinePlayerArgument("player") {
        anyExecutor { executor, args ->
            val player: Deferred<SurfPlayer?> by args

            plugin.launch {
                val surfPlayer = player.await() ?: run {
                    executor.sendText {
                        appendCorePrefix()
                        error("Der Spieler wurde nicht gefunden.")
                    }
                    return@launch
                }

                if (surfPlayer.isOnline()) {
                    executor.sendText {
                        appendCorePrefix()
                        success("Der Spieler ")
                        variableValue(surfPlayer.lastKnownName ?: surfPlayer.uuid.toString())
                        success(" ist aktuell online.")
                    }
                    return@launch
                }

                val lastSeen = surfPlayer.lastSeen ?: run {
                    executor.sendText {
                        appendCorePrefix()
                        error("Der Spieler wurde noch nie auf dem Netzwerk gesehen.")
                    }
                    return@launch
                }


                executor.sendText {
                    appendCorePrefix()
                    info("Der Spieler ")
                    variableValue(surfPlayer.lastKnownName ?: surfPlayer.uuid.toString())
                    info(" wurde zuletzt am ")
                    variableValue(lastSeen.format(dateFormatter))
                    info(" um ")
                    variableValue(lastSeen.format(timeFormatter))
                    info(" gesehen.")
                }
            }
        }
    }
}

private val berlinZone = ZoneId.of("Europe/Berlin")

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(berlinZone)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(berlinZone)