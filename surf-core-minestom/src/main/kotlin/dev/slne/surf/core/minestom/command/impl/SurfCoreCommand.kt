package dev.slne.surf.core.minestom.command.impl

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfProxyServerArgument
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.command.SurfCoreCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions
import dev.slne.surf.core.minestom.SurfCoreMinestomPlugin

fun surfCoreCommand() = commandTree("surfcore") {
    withPermission(CorePermissions.COMMAND_CORE)
    literalArgument("reload") {
        anyExecutor { executor, _ ->
            SurfCoreMinestomPlugin.surfServerConfiguration.reload()
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
