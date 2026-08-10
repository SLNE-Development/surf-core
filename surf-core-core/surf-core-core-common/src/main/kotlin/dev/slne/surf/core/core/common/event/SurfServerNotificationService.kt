package dev.slne.surf.core.core.common.event

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import net.kyori.adventure.audience.Audience

object SurfServerNotificationService {
    fun starting(audiences: Iterable<Audience>, serverName: String) = audiences.forEach { audience ->
        audience.sendText {
            appendStartingPrefix()
            info("Der Server ")
            variableValue(serverName)
            info(" startet nun...")
        }
    }

    fun online(audiences: Iterable<Audience>, serverName: String) = audiences.forEach { audience ->
        audience.sendText {
            appendOnlinePrefix()
            info("Der Server ")
            variableValue(serverName)
            info(" ist nun online.")
        }
    }

    fun stopping(audiences: Iterable<Audience>, serverName: String) = audiences.forEach { audience ->
        audience.sendText {
            appendStoppingPrefix()
            info("Der Server ")
            variableValue(serverName)
            info(" stoppt nun...")
        }
    }

    private fun SurfComponentBuilder.appendStartingPrefix() = append {
        text("»", Colors.YELLOW)
        darkSpacer(" |")
        appendSpace()
    }

    private fun SurfComponentBuilder.appendOnlinePrefix() = append {
        success("»")
        darkSpacer(" |")
        appendSpace()
    }

    private fun SurfComponentBuilder.appendStoppingPrefix() = append {
        error("»")
        darkSpacer(" |")
        appendSpace()
    }
}
