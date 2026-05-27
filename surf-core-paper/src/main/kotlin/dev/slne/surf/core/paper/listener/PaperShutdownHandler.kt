package dev.slne.surf.core.paper.listener

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ServerShutdownHandler
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

@AutoService(ServerShutdownHandler::class)
class PaperShutdownHandler : ServerShutdownHandler {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        val msg = CommonComponents.renderDisconnectMessage(
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
            }
        )

        forEachPlayer { player ->
            player.kick(msg)
        }

        Bukkit.shutdown()
        return true
    }
}