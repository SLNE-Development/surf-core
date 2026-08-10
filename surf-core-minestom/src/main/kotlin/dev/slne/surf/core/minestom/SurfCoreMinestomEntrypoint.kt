package dev.slne.surf.core.minestom

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.minestom.lobby.api.plugin.MinestomPluginEntrypoint
import dev.slne.minestom.lobby.api.plugin.annotation.DataDirectory
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.server.state.SurfServerState
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.config.SurfServerConfiguration
import dev.slne.surf.core.core.common.event.SurfEventBus
import dev.slne.surf.core.core.common.server.SurfServerService
import java.nio.file.Path
import java.time.OffsetDateTime

@Singleton
class SurfCoreMinestomEntrypoint @Inject constructor(
    @DataDirectory path: Path,
) : MinestomPluginEntrypoint {

    init {
        dataPath = path
        surfServerConfiguration = SurfServerConfiguration(path)
    }

    override suspend fun start() {
        validateConfig()

        ClientCoreInstance.clientLoader.onBootstrap()
        ClientCoreInstance.clientLoader.onLoad()
        ClientCoreInstance.clientLoader.connectRedis()

        val server = SurfServer(
            name = surfServerConfig.serverName,
            displayName = surfServerConfig.serverDisplayName,
            category = surfServerConfig.serverCategory,
            state = SurfServerState.STARTING,
            maxPlayers = 0,
            uuid = surfServerConfig.serverUuid,
            startedAt = OffsetDateTime.now(),
        )

        SurfEventBus.fire(SurfServerStartEvent(server.name))
        SurfServerService.addServer(server)

        ClientCoreInstance.clientLoader.onEnable()

        SurfEventBus.fire(SurfServerOnlineEvent(server.name))
        SurfServerService.changeState(server, SurfServerState.RUNNING)
        OfflinePlayerNameCache.startPulling(minestomScope)
    }

    override suspend fun stop() {
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

    companion object {
        lateinit var dataPath: Path
            private set

        lateinit var surfServerConfiguration: SurfServerConfiguration
            private set
    }
}

private fun String.isUnknown() = isBlank() || equals("unknown", ignoreCase = true)

val surfServerConfig get() = SurfCoreMinestomEntrypoint.surfServerConfiguration.config
