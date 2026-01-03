package dev.slne.surf.core.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.core.api.common.event.SurfServerOnlineEvent
import dev.slne.surf.core.api.common.event.SurfServerStoppingEvent
import dev.slne.surf.core.core.common.event.surfEventBus
import dev.slne.surf.core.core.common.redis.redisLoader
import dev.slne.surf.core.paper.command.lastSeenCommand
import dev.slne.surf.core.paper.command.networkListCommand
import dev.slne.surf.core.paper.command.networkTeleportCommand
import dev.slne.surf.core.paper.command.surfCoreCommand
import dev.slne.surf.core.paper.event.SurfServerEventListener
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        surfEventBus.registerListener(SurfServerEventListener)
    }

    override fun onEnable() {
        surfEventBus.fire(SurfServerOnlineEvent(surfServerConfig.serverName))

        lastSeenCommand()
        networkListCommand()
        networkTeleportCommand()
        surfCoreCommand()
    }

    override fun onDisable() {
        surfEventBus.fire(SurfServerStoppingEvent(surfServerConfig.serverName))

        redisLoader.disconnect()
    }
}