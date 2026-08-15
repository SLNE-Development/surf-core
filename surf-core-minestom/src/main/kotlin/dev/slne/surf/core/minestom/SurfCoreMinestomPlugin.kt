package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.core.minestom.command.CoreCommandRegistrar

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
        bindCommandRegistrar<CoreCommandRegistrar>()
        bindEventRegistrar<MinestomPlayerConnectListener>()
    }
}
