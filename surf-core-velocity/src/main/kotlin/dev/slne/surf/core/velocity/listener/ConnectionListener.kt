package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import kotlin.jvm.optionals.getOrNull

object ConnectionListener {
    @Subscribe
    fun onConnected(event: ServerConnectedEvent) {
        val previousServer = event.previousServer.getOrNull()

        if (previousServer != null) {
            val player =
                surfPlayerService.players.find { it.uuid == event.player.uniqueId } ?: return
            player.currentServer = event.server.serverInfo.name

            surfPlayerService.cachePlayer(player)
            return
        }

        plugin.pluginContainer.launch {
            val player = surfPlayerService.getOrLoadOrCreatePlayerByUuid(
                event.player.uniqueId
            ).apply {
                if (firstSeen == null) {
                    firstSeen = System.currentTimeMillis()
                }
                lastSeen = System.currentTimeMillis()
                lastKnownName = event.player.username
                currentServer = event.server.serverInfo.name
            }

            surfPlayerService.cachePlayer(player)

            surfEventBus.fire(
                SurfPlayerConnectEvent(
                    player
                )
            )

            surfPlayerService.savePlayer(player)
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val player = surfPlayerService.findPlayerByUuid(event.player.uniqueId) ?: return

        surfEventBus.fire(
            SurfPlayerDisconnectEvent(
                player
            )
        )

        surfPlayerService.invalidatePlayer(player.uuid)

        plugin.pluginContainer.launch {
            surfPlayerService.savePlayer(player.apply {
                lastSeen = System.currentTimeMillis()
            })
        }
    }
}