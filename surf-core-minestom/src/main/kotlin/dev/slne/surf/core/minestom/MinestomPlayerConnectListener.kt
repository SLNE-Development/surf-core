package dev.slne.surf.core.minestom

import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.minestom.lobby.api.event.EventRegistrar
import dev.slne.minestom.lobby.api.extension.addListener
import dev.slne.minestom.lobby.api.player.LobbyPlayer
import dev.slne.minestom.lobby.api.player.lobbyPlayer
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.command.WhereAmICommandHandler
import dev.slne.surf.core.core.common.player.PlayerConnectionMessages
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.player.error.SurfPlayerErrorService
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import kotlinx.coroutines.launch
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.MinecraftServer
import net.kyori.adventure.text.Component

class MinestomPlayerConnectListener : EventRegistrar {
    override fun register(node: EventNode<Event>) {
        node.addListener<AsyncPlayerConfigurationEvent>(::onPlayerConfiguration)
        node.addListener<PlayerSpawnEvent>(::onPlayerSpawn)
    }

    private fun onPlayerConfiguration(event: AsyncPlayerConfigurationEvent) {
        if (!event.isFirstConfig) return

        val player = event.player
        if (SurfPlayerService.findPlayerByUuid(player.uuid) == null) {
            val errorCode = SurfPlayerErrorService.saveError(
                playerUuid = player.uuid,
                staffMessage = "Player not found in redis after connecting to a Minestom server, most likely a redis issue. Is redis down?",
                scope = minestomScope,
            )
            player.kick(PlayerConnectionMessages.dataLoadFailure(errorCode))
            return
        }

        val hasBypass = (player as? LobbyPlayer)
            ?.hasPermission(CorePermissions.BYPASS_MAX_PLAYERS) == true
        if (!hasBypass && MinecraftServer.getConnectionManager().onlinePlayerCount >= SurfServer.current().maxPlayers) {
            player.kick(Component.text("Der Server ist voll."))
        }
    }

    private fun onPlayerSpawn(event: PlayerSpawnEvent) {
        if (!event.isFirstSpawn) return

        val player = event.lobbyPlayer
        val surfPlayer = SurfPlayerService.findPlayerByUuid(player.uuid) ?: return

        MinestomPlayerDisplayNameService.update(player)

        if (player.hasPermission(CorePermissions.JOIN_WHERE_AM_I)) {
            WhereAmICommandHandler.send(player, player.uuid, player.username)
        }

        if (!surfPlayer.transferred) return

        CoreInstance.redisApi.publishEvent(
            SendPlayerToProxyRequest.Response(
                surfPlayer.uuid,
                SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.SUCCESS),
            )
        )
        minestomScope.launch {
            PlayerProxyConnectionResultWatcher.cleanUp(surfPlayer.uuid)
        }
    }
}
