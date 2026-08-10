package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.paper.command.argument.surfProxyServerArgument
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.command.SurfCoreCommandHandler
import dev.slne.surf.core.paper.PaperBootstrap
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun surfCoreCommand() = commandTree("surfcore") {
    withPermission(PermissionRegistry.COMMAND_CORE)
    literalArgument("reload") {
        anyExecutor { executor, _ ->
            PaperBootstrap.surfServerConfiguration.reload()
            SurfCoreCommandHandler.configReloaded(executor)
        }
    }

    literalArgument("clearinternalplayercache") {
        anyExecutor { executor, _ ->
            SurfCoreCommandHandler.clearInternalPlayerCache(executor)
        }
    }

//    literalArgument("crash") {
//        withRequirement {
//            SurfServer.current().name.contains("dev")
//        }
//
//        anyExecutor { executor, _ ->
//            executor.sendText {
//                appendCorePrefix()
//                error("Der Server wird nun absichtlich zum Testen von Crash-Handling-Funktionen abstürzen...")
//            }
//
//            Runtime.getRuntime().halt(1)
//        }
//    }
//
//    literalArgument("unreachable") {
//        withRequirement {
//            SurfServer.current().name.contains("dev")
//        }
//
//        anyExecutor { executor, _ ->
//            executor.sendText {
//                appendCorePrefix()
//                error("Der Server wird nun absichtlich unerreichbar gemacht, um die Handhabung von Verbindungsproblemen zu testen... o7")
//            }
//
//            while (true) {
//                Thread.sleep(1000)
//            }
//        }
//    }

    literalArgument("testawaitingsend") {
        literalArgument("server") {
            surfBackendServerArgument("backend") {
                playerExecutor { player, args ->
                    val surfPlayer = player.surfPlayer
                    val backend: SurfServer by args

                    plugin.launch {
                        NetworkSendCommandHandler.sendSelf(player, surfPlayer, backend)
                    }
                }
            }
        }

        literalArgument("proxy") {
            surfProxyServerArgument("proxy") {
                playerExecutor { player, args ->
                    val surfPlayer = player.surfPlayer
                    val proxy: SurfProxyServer by args

                    plugin.launch {
                        NetworkSendCommandHandler.sendSelf(player, surfPlayer, proxy)
                    }
                }
            }
        }
    }
}
