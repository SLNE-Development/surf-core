package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.core.api.paper.command.argument.permissionSurfServerArgument
import dev.slne.surf.core.api.paper.util.toSurfPlayer
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkServerCommand() = commandTree("nserver") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER)

    permissionSurfServerArgument("server") {
        playerExecutor { player, args ->
            val server: CommonSurfServer by args
            val surfPlayer = player.toSurfPlayer()

            player.sendText {
                appendInfoPrefix()
                info("Du wirst zum Server ")
                variableValue(server.name)
                info(" gesendet...")
            }

            when (val commonServer = server) {
                is SurfProxyServer -> {
                    surfPlayer.send(commonServer)
                }

                is SurfServer -> {
                    plugin.launch {
                        val status = surfCoreApi.sendPlayerAwaiting(surfPlayer, commonServer)

                        if (status.isSuccessful()) {
                            player.sendText {
                                appendSuccessPrefix()
                                success("Du wurdest erfolgreich zum Server ")
                                variableValue(server.name)
                                success(" gesendet!")
                            }
                        } else {
                            player.sendText {
                                appendErrorPrefix()
                                error("Du konntest nicht zum Server verbunden werden ")

                                status.velocityMessage.let {
                                    if (it != null) {
                                        error(": ")
                                        append(it)
                                    } else {
                                        error(".")
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