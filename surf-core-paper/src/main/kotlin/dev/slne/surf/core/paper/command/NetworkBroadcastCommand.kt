package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.paper.permission.PermissionRegistry
import net.kyori.adventure.text.minimessage.MiniMessage

fun networkBroadcastCommand() = commandTree("nbroadcast") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_BROADCAST)
    greedyStringArgument("message") {
        anyExecutor { executor, args ->
            val message: String by args

            SurfCoreApi.getOnlinePlayers().forEach {
                it.sendText {
                    appendInfoPrefix()
                    append(MiniMessage.miniMessage().deserialize(message))
                }
            }

            executor.sendText {
                appendCorePrefix()
                success("Die Nachricht wurde an ")
                variableValue(SurfCoreApi.getOnlinePlayers().size)
                success(" Spieler gesendet.")
            }
        }
    }
}