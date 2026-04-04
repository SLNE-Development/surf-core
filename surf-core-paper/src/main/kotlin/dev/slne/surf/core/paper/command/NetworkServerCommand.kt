package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.permissionSurfServerArgument
import dev.slne.surf.core.api.paper.util.toSurfPlayer
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin

fun networkServerCommand() = commandTree("nserver") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER)

    permissionSurfServerArgument("server") {
        playerExecutor { player, args ->
            val server: CommonSurfServer by args
            val surfPlayer = player.toSurfPlayer()

            player.sendText {
                appendCorePrefix()
                info("Du wirst zum Server ")
                variableValue(server.name)
                info(" gesendet...")
            }

            when (val commonServer = server) {
                is SurfProxyServer -> {
                    plugin.launch {
                        val status = SurfCoreApi.sendPlayerAwaiting(surfPlayer, commonServer)

                        if (status.isSuccessful()) {
                            player.sendText {
                                appendCorePrefix()
                                success("Du wurdest erfolgreich zum Server ")
                                variableValue(server.name)
                                success(" gesendet!")
                            }
                        } else {
                            player.sendText {
                                appendCorePrefix()
                                error("Du konntest nicht zum Server verbunden werden: ${status.status}")
                            }
                        }
                    }
                }

                is SurfServer -> {
                    plugin.launch {
                        val status = SurfCoreApi.sendPlayerAwaiting(surfPlayer, commonServer)

                        if (status.isSuccessful()) {
                            player.sendText {
                                appendCorePrefix()
                                success("Du wurdest erfolgreich zum Server ")
                                variableValue(server.name)
                                success(" gesendet!")
                            }
                        } else {
                            player.sendText {
                                appendCorePrefix()
                                error("Du konntest nicht zum Server verbunden werden")

                                status.velocityMessage.let {
                                    if (it != null) {
                                        error(": ")
                                        append(it)
                                    } else {
                                        error(": ${status.status}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}