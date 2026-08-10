package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.niceRed
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

object NetworkBroadcastCommandHandler {
    fun broadcast(audience: Audience, serializedMessage: String) =
        broadcast(audience, miniMessage.deserialize(serializedMessage))

    fun broadcast(audience: Audience, message: Component) {
        val players = SurfCoreApi.getOnlinePlayers()
        players.forEach { player ->
            player.sendText {
                appendCorePrefix()
                appendNewline()
                appendCorePrefix()
                niceRed("INFO: ", TextDecoration.BOLD)
                append(message)
                appendNewline()
                appendCorePrefix()
            }
        }
        audience.sendText {
            appendCorePrefix()
            success("Die Nachricht wurde an ")
            variableValue(players.size)
            success(" Spieler gesendet.")
        }
    }
}
