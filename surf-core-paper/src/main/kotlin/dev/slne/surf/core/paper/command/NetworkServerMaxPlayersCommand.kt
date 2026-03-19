package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.command.argument.surfBackendServerArgument
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.redis.event.SurfServerChangeMaxPlayersRedisEvent
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun networkServerMaxPlayersCommand() = commandTree("nmaxplayers") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_SERVER_MAX_PLAYERS)

    surfBackendServerArgument("backend") {
        integerArgument("maxPlayers") {
            anyExecutor { sender, args ->
                val backend: CommonSurfServer by args
                val maxPlayers: Int by args

                CoreInstance.redisApi.publishEvent(
                    SurfServerChangeMaxPlayersRedisEvent(
                        backend,
                        maxPlayers
                    )
                )

                sender.sendText {
                    appendSuccessPrefix()
                    success("Die maximale Spieleranzahl des Servers ")
                    variableValue("'${backend.name}'")
                    success(" wurde auf ")
                    variableValue(maxPlayers)
                    success(" gesetzt.")
                }
            }
        }
    }
}