package dev.slne.surf.core.minestom.command

import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.command.CommandRegistrar
import dev.slne.surf.core.minestom.command.impl.*

@Singleton
class CoreCommandRegistrar : CommandRegistrar {
    override fun register() {
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
    }
}