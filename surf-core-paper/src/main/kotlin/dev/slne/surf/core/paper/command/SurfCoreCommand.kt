package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.core.api.paper.command.argument.surfProxyServerArgument
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.client.player.resource.PlayerResourceCache
import dev.slne.surf.core.core.common.command.NetworkSendCommandHandler
import dev.slne.surf.core.core.common.command.SurfCoreCommandHandler
import dev.slne.surf.core.paper.PaperBootstrap
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import kotlin.system.measureTimeMillis

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
            PlayerResourceCache.invalidateAll()
        }
    }

    literalArgument("resource") {
        playerExecutorSuspend { player, _ ->
            val resourceUuidMillis = measureTimeMillis {
                val resourceUuid = PlayerResource.fromPlayer(player.uniqueId)

                player.sendText {
                    appendSuccessPrefix()
                    success("Resource by UUID: ${resourceUuid?.playerUuid}, ${resourceUuid?.username}, ${resourceUuid?.skin}")
                }
            }

            val resourceNameMillis = measureTimeMillis {
                val resourceName = PlayerResource.fromPlayer(player.name)

                player.sendText {
                    appendSuccessPrefix()
                    success("Resource by Name: ${resourceName?.playerUuid}, ${resourceName?.username}, ${resourceName?.skin}")
                }
            }

            player.sendText {
                appendSuccessPrefix()
                success("Resource by UUID took: ${resourceUuidMillis}ms, Resource by Name took: ${resourceNameMillis}ms")
            }
        }
    }

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
