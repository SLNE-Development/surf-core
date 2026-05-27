package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.args.MiniMessageArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.niceRed
import dev.slne.surf.core.paper.permission.PermissionRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

fun networkBroadcastCommand() = commandTree("nbroadcast") {
    withPermission(PermissionRegistry.COMMAND_NETWORK_BROADCAST)
    argument(MiniMessageArgument("message")) {
        anyExecutor { executor, args ->
            val message: Component by args

            SurfCoreApi.getOnlinePlayers().forEach {
                it.sendText {
                    appendCorePrefix()

                    appendNewline()
                    appendCorePrefix()
                    niceRed("INFO: ", TextDecoration.BOLD)
                    append(message)

                    appendNewline()
                    appendCorePrefix()
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