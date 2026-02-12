package dev.slne.surf.core.velocity.listener

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPreShutdownEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.type.SurfServerType
import dev.slne.surf.core.api.velocity.util.surfPlayer
import dev.slne.surf.core.core.common.server.surfServerService
import dev.slne.surf.core.velocity.plugin
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

@Suppress("UnstableApiUsage")
object VelocityServerListener {
    @Subscribe(priority = Short.MIN_VALUE)
    fun onPreShutdown(event: ProxyPreShutdownEvent, continuation: Continuation) {
        val currentProxy = SurfServer.current()

        val targetProxies = surfServerService.servers
            .filter { it.type == SurfServerType.PROXY && it.name != currentProxy.name }
            .sortedBy { it.getPlayerCount() }

        plugin.proxy.allPlayers.forEach { player ->
            val target = targetProxies.firstOrNull { it.getPlayerCount() < it.maxPlayers }

            if (target == null) {
                player.disconnect(buildText {
                    CommonComponents.renderDisconnectMessage(
                        this,
                        "DER PROXY WIRD HERUNTERGEFAHREN",
                        {
                            error("Es wurde kein anderer Proxy Server gefunden, zu dem du wechseln könntest.")
                        },
                        false
                    )
                })
                return@forEach
            }

            target.pullPlayers(player.surfPlayer)
        }

        continuation.resume()
    }
}