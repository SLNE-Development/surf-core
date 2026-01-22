package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.paper.PaperBootstrap
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun surfCoreCommand() = commandTree("core") {
    withPermission(PermissionRegistry.COMMAND_CORE)
    literalArgument("reload") {
        anyExecutor { executor, _ ->
            PaperBootstrap.surfServerConfigHolder.reload()

            executor.sendText {
                appendSuccessPrefix()
                success("Die Konfiguration wurde neu geladen.")
            }
        }
    }

    literalArgument("clearinternalplayercache") {
        anyExecutor { executor, _ ->
            surfPlayerService.clearPlayers()

            executor.sendText {
                appendSuccessPrefix()
                success("Die Spieler-Caches wurden geleert.")
            }
        }
    }
}