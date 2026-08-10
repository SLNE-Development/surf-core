package dev.slne.surf.core.minestom.command

import dev.slne.minestom.lobby.api.command.CommandPermission
import dev.slne.minestom.lobby.api.coroutine.minestomScope
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.command.SurfCoreCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.minestom.SurfCoreMinestomEntrypoint
import dev.slne.surf.core.minestom.minestomCoreConfig
import kotlinx.coroutines.launch
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor

class SurfCoreCommand : SurfCoreMinestomCommand() {
    @Command("surfcore reload")
    @CommandPermission(CorePermissions.COMMAND_CORE)
    fun reload(actor: MinestomCommandActor) {
        SurfCoreMinestomEntrypoint.surfServerConfiguration.reload()
        SurfCoreMinestomEntrypoint.minestomCoreConfigManager.reload()
        SurfServerService.addServer(
            SurfServer.current().copy(maxPlayers = minestomCoreConfig.maxPlayers)
        )
        SurfCoreCommandHandler.configReloaded(actor.sender())
    }

    @Command("surfcore clearinternalplayercache")
    @CommandPermission(CorePermissions.COMMAND_CORE)
    fun clearInternalPlayerCache(actor: MinestomCommandActor) =
        SurfCoreCommandHandler.clearInternalPlayerCache(actor.sender())

    @Command("surfcore testawaitingsend server")
    @CommandPermission(CorePermissions.COMMAND_CORE)
    fun testServer(actor: MinestomCommandActor, targetServer: SurfServer) {
        val player = actor.requirePlayer()
        minestomScope.launch {
            NetworkSendCommandHandler.sendSelf(player, player.surfPlayer, targetServer)
        }
    }

    @Command("surfcore testawaitingsend proxy")
    @CommandPermission(CorePermissions.COMMAND_CORE)
    fun testProxy(actor: MinestomCommandActor, targetProxy: SurfProxyServer) {
        val player = actor.requirePlayer()
        minestomScope.launch {
            NetworkSendCommandHandler.sendSelf(player, player.surfPlayer, targetProxy)
        }
    }
}
