package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object LastSeenCommandHandler {
    private val berlinZone = ZoneId.of("Europe/Berlin")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(berlinZone)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(berlinZone)

    fun send(audience: Audience, player: SurfPlayer?) {
        if (player == null) {
            audience.sendText {
                appendCorePrefix()
                error("Der Spieler wurde nicht gefunden.")
            }
            return
        }
        if (player.isOnline()) {
            audience.sendText {
                appendCorePrefix()
                success("Der Spieler ")
                variableValue(player.username)
                success(" ist aktuell online.")
            }
            return
        }
        val lastSeen = player.lastSeen
        if (lastSeen == null) {
            audience.sendText {
                appendCorePrefix()
                error("Der Spieler wurde noch nie auf dem Netzwerk gesehen.")
            }
            return
        }
        audience.sendText {
            appendCorePrefix()
            info("Der Spieler ")
            variableValue(player.username)
            info(" wurde zuletzt am ")
            variableValue(lastSeen.format(dateFormatter))
            info(" um ")
            variableValue(lastSeen.format(timeFormatter))
            info(" gesehen.")
        }
    }
}
