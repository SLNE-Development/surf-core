package dev.slne.surf.core.minestom.command.impl

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutor
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfProxyServerArgument
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.command.SurfCoreCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.minestom.SurfCoreMinestomEntrypoint
import dev.slne.surf.core.minestom.minestomCoreConfig

fun surfCoreCommand() = commandTree("surfcore") {
    withPermission(CorePermissions.COMMAND_CORE)
    literalArgument("reload") {
        anyExecutor { executor, _ ->
            SurfCoreMinestomEntrypoint.surfServerConfiguration.reload()
            SurfCoreMinestomEntrypoint.minestomCoreConfigManager.reload()
            SurfServerService.addServer(
                SurfServer.current().copy(maxPlayers = minestomCoreConfig.maxPlayers)
            )
            SurfCoreCommandHandler.configReloaded(executor)
        }
    }

    literalArgument("clearinternalplayercache") {
        anyExecutor { executor, _ ->
            SurfCoreCommandHandler.clearInternalPlayerCache(executor)
        }
    }


    literalArgument("testawaitingsend") {
        literalArgument("server") {
            surfBackendServerArgument("backend") {
                playerExecutorSuspend { player, args ->
                    val surfPlayer = player.surfPlayer
                    val backend: SurfServer by args

                    NetworkSendCommandHandler.sendSelf(player, surfPlayer, backend)
                }
            }
        }

        literalArgument("proxy") {
            surfProxyServerArgument("proxy") {
                playerExecutorSuspend { player, args ->
                    val surfPlayer = player.surfPlayer
                    val proxy: SurfProxyServer by args

                    NetworkSendCommandHandler.sendSelf(player, surfPlayer, proxy)
                }
            }
        }
    }
}
