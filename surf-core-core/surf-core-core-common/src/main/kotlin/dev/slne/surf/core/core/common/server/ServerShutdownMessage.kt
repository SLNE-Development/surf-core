package dev.slne.surf.core.core.common.server

import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component

object ServerShutdownMessage {
    fun create(reason: Component?) = CommonComponents.renderDisconnectMessage(
        SurfComponentBuilder(),
        "DER SERVER WIRD HERUNTERGEFAHREN.",
        {
            spacer("Der Server wurde gestoppt. Bitte versuche es später erneut.")
            reason?.let {
                spacer("Grund: ")
                append(it)
            }
        },
        {
            appendDiscordLink()
        },
    )
}
