package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerConnectEvent
import dev.slne.surf.core.api.common.event.SurfPlayerDisconnectEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.velocity.plugin
import kotlin.jvm.optionals.getOrNull

object ConnectionListener {
    @Subscribe(priority = Short.MIN_VALUE)
    fun onLogin(event: PlayerChooseInitialServerEvent) {
        val newServer = event.initialServer.getOrNull()?.serverInfo?.name ?: return
        println("[connection: new] ${event.player.username} (${event.player.remoteAddress}) connected to '$newServer'")

        plugin.pluginContainer.launch {
            val player = surfPlayerService.getOrLoadOrCreatePlayerByUuid(
                event.player.uniqueId
            ).apply {
                if (firstSeen == null) {
                    firstSeen = System.currentTimeMillis()
                }
                lastSeen = System.currentTimeMillis()
                lastKnownName = event.player.username
                currentServer = newServer
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
    fun onConnected(event: ServerPostConnectEvent) {
        val previousServer = event.previousServer
        val newServer = event.player.currentServer.getOrNull()?.serverInfo?.name

        if (previousServer != null) {
            val player =
                surfPlayerService.players.find { it.uuid == event.player.uniqueId } ?: return
            player.currentServer = newServer

            println("[connection: update] ${event.player.username} was redirected from '${previousServer.serverInfo.name}' to '$newServer'")

            surfPlayerService.cachePlayer(player)
        }
    }

    @Subscribe
    fun onKick(event: KickedFromServerEvent) {
        when (event.result) {
            is KickedFromServerEvent.DisconnectPlayer -> {
                val player = surfPlayerService.findPlayerByUuid(event.player.uniqueId) ?: return

                surfEventBus.fire(
                    SurfPlayerDisconnectEvent(
                        player
                    )
                )

                surfPlayerService.invalidatePlayer(player.uuid)

                println("[connection: closed] ${event.player.username} disconnected")

                plugin.pluginContainer.launch {
                    surfPlayerService.savePlayer(player.apply {
                        lastSeen = System.currentTimeMillis()
                    })
                }
            }

            else -> {}
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

        println("[connection: closed] ${event.player.username} disconnected")

        plugin.pluginContainer.launch {
            surfPlayerService.savePlayer(player.apply {
                lastSeen = System.currentTimeMillis()
            })
        }
    }
}