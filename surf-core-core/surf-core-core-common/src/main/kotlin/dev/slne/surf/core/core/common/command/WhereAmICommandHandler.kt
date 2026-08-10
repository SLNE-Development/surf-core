package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.formatMillis
import net.kyori.adventure.audience.Audience
import java.util.UUID

object WhereAmICommandHandler {
    fun send(audience: Audience, uuid: UUID, username: String) {
        val player = SurfPlayerService.findPlayerByUuid(uuid)
        if (player == null) {
            audience.sendText {
                appendCorePrefix()
                error("Deine Spielerdaten konnten nicht geladen werden.")
            }
            return
        }
        val now = System.currentTimeMillis()
        audience.sendText {
            appendCorePrefix()
            info("Du, ")
            append {
                variableValue(username)
                hoverEvent(buildText { variableValue(uuid.toString()) })
                clickCopiesToClipboard(uuid.toString())
            }
            info(", befindest dich momentan, ")
            append {
                spacer("(${now.formatMillis()})")
                clickCopiesToClipboard(now.toString())
            }
            info(", auf dem Server ")
            variableValue(player.currentServerName ?: "Unbekannt")
            info(" auf dem Proxy ")
            variableValue(player.currentProxyName ?: "Unbekannt")
            info(".")
        }
    }
}
