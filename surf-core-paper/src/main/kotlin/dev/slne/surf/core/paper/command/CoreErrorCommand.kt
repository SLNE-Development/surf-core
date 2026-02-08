package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.core.common.player.surfCoreErrorLoggingService
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.clickRunsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter

fun coreErrorCommand() = commandTree("coreerror") {
    withPermission(PermissionRegistry.COMMAND_CORE_ERROR)
    literalArgument("viewPlayer") {
        stringArgument("playerName") {
            anyExecutorSuspend { executor, args ->
                val playerName: String by args
                val player = PlayerLookupService.getUuid(playerName) ?: run {
                    executor.sendText {
                        appendErrorPrefix()
                        error("Der Spieler wurde nicht gefunden.")
                    }
                    return@anyExecutorSuspend
                }

                val error = surfCoreErrorLoggingService.getErrors(player)

                executor.sendText {
                    appendNewline()
                    append(pagination.renderComponent(error))
                }
            }
        }
    }
    literalArgument("viewCode") {
        stringArgument("code") {
            anyExecutorSuspend { executor, args ->
                val code: String by args
                val error = surfCoreErrorLoggingService.getError(code) ?: run {
                    executor.sendText {
                        appendErrorPrefix()
                        error("Der Fehler wurde nicht gefunden.")
                    }
                    return@anyExecutorSuspend
                }

                executor.sendText {
                    appendNewline()
                    darkSpacer("*" + "-".repeat(20) + "*")
                    appendNewline()
                    appendNewline()
                    appendInfoPrefix()
                    info("Spieler: ")
                    append {
                        variableValue(error.playerUuid.toString())
                        clickCopiesToClipboard(error.playerUuid.toString())
                    }
                    appendNewline()
                    appendInfoPrefix()
                    info("Fehlercode: ")
                    variableValue(error.code)
                    appendNewline()
                    appendInfoPrefix()
                    info("Nachricht: ")
                    variableValue(error.message)
                    appendNewline()
                    appendInfoPrefix()
                    info("Server: ")
                    variableValue(error.server)
                    appendNewline()
                    appendInfoPrefix()
                    info("Ort: ")
                    variableValue(error.location)
                    appendNewline()
                    appendInfoPrefix()
                    info("Erstmals aufgetreten: ")
                    variableValue(error.timestamp.format(dateTimeFormatter))
                    appendNewline()
                    appendInfoPrefix()
                    info("Zuletzt aufgetreten: ")
                    variableValue(error.lastOccurred.format(dateTimeFormatter))
                    appendNewline()
                    appendInfoPrefix()
                    info("Anzahl: ")
                    variableValue(error.occurrenceCount.toString())
                    appendNewline()
                    appendInfoPrefix()
                    info("Stacktrace: ")
                    appendNewline()
                    spacer(error.stacktrace.take(500))
                    if (error.stacktrace.length > 500) {
                        spacer("... (gekürzt)")
                    }
                    appendNewline()
                    appendNewline()
                    darkSpacer("*" + "-".repeat(20) + "*")
                }
            }
        }
    }
}

private val pagination = Pagination<SurfCoreError> {
    title { primary("Fehler-Übersicht") }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                appendInfoPrefix()
                variableKey("Fehlercode: ")
                variableValue(row.code)
                appendSpace()
                spacer("(${row.timestamp.format(dateTimeFormatter)})")
                clickRunsCommand("/coreerror viewCode ${row.code}")
                hoverEvent(buildText {
                    spacer("Klicke, um Details zu diesem Fehler anzuzeigen.")
                })
            }
        )
    }
}