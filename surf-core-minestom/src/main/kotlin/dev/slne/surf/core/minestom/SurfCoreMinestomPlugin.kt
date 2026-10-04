package dev.slne.surf.core.minestom

import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.player.PlayerLimit
import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.config.SurfServerConfiguration
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.minestom.command.impl.*
import dev.slne.surf.core.minestom.listener.MinestomPlayerConnectListener
import dev.slne.surf.core.minestom.listener.MinestomSurfServerEventListener
import dev.slne.surf.core.minestom.listener.redis.MinestomRedisListener
import dev.slne.surf.core.minestom.listener.redis.MinestomTeleportRedisListener
import java.time.OffsetDateTime

object SurfCoreMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        validateConfig()

        ClientCoreInstance.clientLoader.onBootstrap()
        ClientCoreInstance.clientLoader.onLoad()

        SurfEventBus.registerListener(MinestomSurfServerEventListener)
        CoreInstance.redisApi.subscribeToEvents(MinestomTeleportRedisListener)
        ClientCoreInstance.clientLoader.withListener(MinestomRedisListener)
        ClientCoreInstance.clientLoader.connectRedis()

        val server = SurfServer(
            name = surfServerConfig.serverName,
            displayName = surfServerConfig.serverDisplayName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            maxPlayers = PlayerLimit.maxPlayers ?: 0,
            uuid = surfServerConfig.serverUuid,
            startedAt = OffsetDateTime.now(),
        )

        SurfEventBus.fire(SurfServerStartEvent(server.name))
        SurfServerService.addServer(server)

        ClientCoreInstance.clientLoader.onEnable()
        MinestomPlayerDisplayNameService.start()

        SurfEventBus.fire(SurfServerOnlineEvent(server.name))
        SurfServerService.changeState(server, SurfServerState.RUNNING)
        OfflinePlayerNameCache.startPulling(minestomScope)
    }

    override suspend fun onEnable() {
        hubCommand()
        lastSeenCommand()
        networkBroadcastCommand()
        networkInformationCommand()
        networkListCommand()
        networkSendCommand()
        networkServerCommand()
        networkServerMaxPlayersCommand()
        networkTeleportCommand()
        surfCoreCommand()
        whereAmICommand()

        MinestomPlayerConnectListener.register(eventNode)
    }

    override suspend fun onDisable() {
        MinestomPlayerDisplayNameService.stop()
        val server = SurfServerService.getServerByName(surfServerConfig.serverName)

        if (server != null) {
            SurfEventBus.fire(SurfServerStoppingEvent(server.name))
            SurfServerService.changeState(server, SurfServerState.STOPPING)
            SurfServerService.removeServer(server)
        }

        ClientCoreInstance.clientLoader.onDisable()
    }

    private fun validateConfig() {
        require(!surfServerConfig.serverName.isUnknown()) {
            "The Minestom surf-core server name must be configured"
        }
        require(!surfServerConfig.serverDisplayName.isUnknown()) {
            "The Minestom surf-core server display name must be configured"
        }
        require(!surfServerConfig.serverCategory.isUnknown()) {
            "The Minestom surf-core server category must be configured"
        }
    }

    var surfServerConfiguration = SurfServerConfiguration(dataDirectory)
}

private fun String.isUnknown() = isBlank() || equals("unknown", ignoreCase = true)

val surfServerConfig get() = SurfCoreMinestomPlugin.surfServerConfiguration.config
