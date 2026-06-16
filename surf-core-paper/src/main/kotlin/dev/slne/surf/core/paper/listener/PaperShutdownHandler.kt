package dev.slne.surf.core.paper.listener

import com.google.auto.service.AutoService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.redis.listener.ServerShutdownHandler
import org.bukkit.Bukkit

@AutoService(ServerShutdownHandler::class)
class PaperShutdownHandler : ServerShutdownHandler {
    override fun shutdown(commonSurfServer: CommonSurfServer, reason: Component?): Boolean? {
        if (commonSurfServer.uuid != SurfServer.current().uuid) {
            return null
        }

        val msg = Component.text()
            .append(Component.text("Hexoria Network").color(NamedTextColor.GREEN))
            .decorate(TextDecoration.BOLD)
            .append(Component.newline())
            .append(Component.newline())
            .append(
                Component.text("DER SERVER WIRD HERUNTERGEFAHREN.")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
            )
            .append(Component.newline())
            .append(
                Component.text("Der Server wurde gestoppt. Bitte versuche es später erneut.")
                    .color(NamedTextColor.GRAY)
            )
            .also { builder ->
                reason?.let {
                    builder.append(Component.newline())
                    builder.append(Component.text("Grund: ").color(NamedTextColor.GRAY))
                    builder.append(it)
                }
            }
            .build()

        forEachPlayer { player ->
            player.kick(msg)
        }

        Bukkit.shutdown()
        return true
    }
}