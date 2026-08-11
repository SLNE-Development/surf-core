package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.core.minestom.command.*
import dev.slne.surf.core.minestom.command.impl.HubCommand
import dev.slne.surf.core.minestom.command.impl.LastSeenCommand
import dev.slne.surf.core.minestom.command.impl.NetworkBroadcastCommand
import dev.slne.surf.core.minestom.command.impl.NetworkInformationCommand
import dev.slne.surf.core.minestom.command.impl.NetworkListCommand
import dev.slne.surf.core.minestom.command.impl.NetworkSendCommand
import dev.slne.surf.core.minestom.command.impl.NetworkServerCommand
import dev.slne.surf.core.minestom.command.impl.NetworkServerMaxPlayersCommand
import dev.slne.surf.core.minestom.command.impl.NetworkTeleportCommand
import dev.slne.surf.core.minestom.command.impl.SurfCoreCommand
import dev.slne.surf.core.minestom.command.impl.WhereAmICommand

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
