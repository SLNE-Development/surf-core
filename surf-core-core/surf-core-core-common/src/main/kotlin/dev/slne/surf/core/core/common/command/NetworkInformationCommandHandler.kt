package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.formatDateTime
import net.kyori.adventure.audience.Audience

object NetworkInformationCommandHandler {
    fun sendNetworkInformation(audience: Audience) = audience.sendText {
        appendCorePrefix()
        info("Derzeit sind ")
        variableValue(SurfPlayerService.players.size)
        info(" Spieler verteilt auf ")
        variableValue(SurfServerService.servers.size)
        info(" Server online.")
    }

    fun sendPlayerInformation(audience: Audience, target: SurfPlayer) = audience.sendText {
        appendNewline()
        darkSpacer("» | ")
        variableValue(target.lastKnownName ?: target.uuid.toString())
        appendNewline(2)
        darkSpacer("» | ")
        variableKey("Erstes mal gesehen:")
        appendNewline()
        darkSpacer("» | ")
        variableValue(target.firstSeen?.formatDateTime() ?: "/")
        appendNewline(2)
        darkSpacer("» | ")
        variableKey("Aktueller Server:")
        appendNewline()
        darkSpacer("» | ")
        variableValue(target.currentServerName ?: "Unbekannt")
        appendNewline(2)
        darkSpacer("» | ")
        variableKey("Aktueller Proxy:")
        appendNewline()
        darkSpacer("» | ")
        variableValue(target.currentProxyName ?: "Unbekannt")
        appendNewline(2)
        darkSpacer("» | ")
        variableKey("Letzte Ip-Adresse:")
        appendNewline()
        darkSpacer("» | ")
        variableValue(target.lastKnownIpAddress?.toString() ?: "Unbekannt")
    }

    fun sendServerInformation(audience: Audience, server: CommonSurfServer) = audience.sendText {
        appendNewline()
        darkSpacer("» | ")
        variableValue(server.name)
        appendNewline(2)
        darkSpacer("» | ")
        variableKey("Kategorie: ")
        variableValue(server.category)
        appendNewline()
        darkSpacer("» | ")
        variableKey("Status: ")
        variableValue(server.state.toString())
        appendNewline()
        darkSpacer("» | ")
        variableKey("Spieler: ")
        variableValue("${server.getPlayerCount()}/${server.maxPlayers}")
    }
}
