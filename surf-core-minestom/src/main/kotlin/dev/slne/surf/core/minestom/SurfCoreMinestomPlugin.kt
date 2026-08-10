package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.core.minestom.command.*

@AutoService(MinestomPlugin::class)
@MinestomPluginMeta(
    "surf-core-minestom",
    dependsOn = [
        "surf-api-minestom",
        "surf-redis-minestom",
        "surf-rabbitmq-minestom",
    ]
)
class SurfCoreMinestomPlugin : MinestomPlugin(SurfCoreMinestomEntrypoint::class.java) {
    override fun configurePlugin() {
        bindCommandRegistrar<SurfCoreCommandTypeRegistrar>()
        bindCommandRegistrar<LastSeenCommand>()
        bindCommandRegistrar<NetworkListCommand>()
        bindCommandRegistrar<NetworkInformationCommand>()
        bindCommandRegistrar<NetworkBroadcastCommand>()
        bindCommandRegistrar<NetworkSendCommand>()
        bindCommandRegistrar<NetworkServerCommand>()
        bindCommandRegistrar<NetworkServerMaxPlayersCommand>()
        bindCommandRegistrar<NetworkTeleportCommand>()
        bindCommandRegistrar<HubCommand>()
        bindCommandRegistrar<WhereAmICommand>()
        bindCommandRegistrar<SurfCoreCommand>()
        bindEventRegistrar<MinestomPlayerConnectListener>()
    }
}
