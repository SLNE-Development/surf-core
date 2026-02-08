package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.longArgument
import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.clickRunsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter

fun surfCoreSystemErrorCommand() = commandTree("surfcoresystemerror") {
    withPermission(PermissionRegistry.COMMAND_CORE_ERROR)
    literalArgument("list") {
        anyExecutorSuspend { executor, _ ->
            val errors = surfCoreSystemErrorService.getAllErrors()

            executor.sendText {
                appendNewline()
                append(pagination.renderComponent(errors))
            }
        }
    }
    literalArgument("view") {
        longArgument("id") {
            anyExecutorSuspend { executor, args ->
                val id: Long by args
                val error = surfCoreSystemErrorService.getError(id) ?: run {
                    executor.sendText {
                        appendErrorPrefix()
                        error("Der Fehler wurde nicht gefunden.")
                    }
                    return@anyExecutorSuspend
                }

                executor.sendText {
                    appendNewline()
                    darkSpacer("*" + "-".repeat(40) + "*")
                    appendNewline()
                    appendNewline()
                    appendInfoPrefix()
                    info("Fehler-ID: ")
                    variableValue(error.id.toString())
                    appendNewline()
                    appendInfoPrefix()
                    info("Server: ")
                    variableValue(error.server)
                    appendNewline()
                    appendInfoPrefix()
                    info("Ort: ")
                    append {
                        variableValue(error.location)
                        clickCopiesToClipboard(error.location)
                    }
                    appendNewline()
                    appendInfoPrefix()
                    info("Erstmals aufgetreten: ")
                    variableValue(error.firstOccurred.format(dateTimeFormatter))
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
                    info("Nachricht: ")
                    appendNewline()
                    spacer(error.errorMessage.take(200))
                    if (error.errorMessage.length > 200) {
                        spacer("... (gekürzt)")
                    }
                    appendNewline()
                    appendInfoPrefix()
                    info("Stacktrace: ")
                    appendNewline()
                    spacer(error.stacktrace.take(1000))
                    if (error.stacktrace.length > 1000) {
                        appendNewline()
                        spacer("... (gekürzt, ${error.stacktrace.length} Zeichen total)")
                    }
                    appendNewline()
                    appendNewline()
                    darkSpacer("*" + "-".repeat(40) + "*")
                }
            }
        }
    }
}

private val pagination = Pagination<SystemError> {
    title { primary("System-Fehler Übersicht") }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                appendInfoPrefix()
                variableKey("ID: ")
                variableValue(row.id.toString())
                appendSpace()
                spacer("(${row.occurrenceCount}x)")
                appendSpace()
                spacer("- ${row.location.take(50)}")
                if (row.location.length > 50) {
                    spacer("...")
                }
                clickRunsCommand("/systemerror view ${row.id}")
                hoverEvent(buildText {
                    spacer("Klicke, um Details zu diesem Fehler anzuzeigen.")
                    appendNewline()
                    spacer("Zuletzt: ${row.lastOccurred.format(dateTimeFormatter)}")
                })
            }
        )
    }
}
