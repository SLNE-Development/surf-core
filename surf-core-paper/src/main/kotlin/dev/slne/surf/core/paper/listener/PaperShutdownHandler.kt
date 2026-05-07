package dev.slne.surf.core.paper.listener

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.buildText
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

        val msg = buildDisconnectComponent(reason)

        forEachPlayer { player ->
            player.kick(msg)
        }

        Bukkit.shutdown()
        return true
    }


    private fun buildDisconnectComponent(reason: Component?) = buildText {
        appendNewline(2)
        primary("CASTCRAFTER")
        appendNewline()
        primary("COMMUNITY SERVER")
        appendNewline(2)
        error("DER SERVER WIRD HERUNTERGEFAHREN.")
        appendNewline(3)
        spacer("Der Server wurde gestoppt. Bitte versuche es später erneut.")
        reason?.let {
            appendNewline()
            spacer("Grund: ")
            append(reason)
        }
        appendNewline(2)
        primary("discord.gg/castcrafter")
    }
}