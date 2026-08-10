package dev.slne.surf.core.core.common.player

import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.core.core.common.util.niceRed

object PlayerConnectionMessages {
    fun dataLoadFailure(errorCode: String) = CommonComponents.renderDisconnectMessage(
        SurfComponentBuilder(),
        "DEINE SPIELERDATEN KONNTEN NICHT GELADEN WERDEN.",
        {
            spacer("Fehlercode: ")
            niceRed(errorCode)
            appendNewline()
            error("Internal Server error. Data Transmitter or holder may be down?")
            appendNewline(3)
            spacer("Beim laden deiner Spielerdaten ist ein interner Fehler aufgetreten.")
        },
        issue = true,
    )
}
