package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.uuidArgument
import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.clickRunsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

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
        uuidArgument("uuid") {
            anyExecutorSuspend { executor, args ->
                val uuid: UUID by args
                val error = surfCoreSystemErrorService.getError(uuid) ?: run {
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
                    primary("Systemfehler Details", TextDecoration.BOLD)
                    appendNewline()
                    appendNewline()
                    appendInfoPrefix()
                    info("Fehler-Uuid: ")
                    variableValue(error.uuid.toString())
                    appendNewline()
                    appendInfoPrefix()
                    info("Server: ")
                    variableValue(error.server)
                    appendNewline()
                    appendInfoPrefix()
                    info("Ort: ")
                    append {
                        variableValue(error.getLocationClassName())
                        hoverEvent(buildText {
                            variableValue(error.location)
                            appendNewline()
                            spacer("Klicke, um den vollständigen Ort zu kopieren.")
                        })
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
                    info("Anzahl in den letzten 24h: ")
                    variableValue(error.occurrenceCount.toString())
                    appendNewline()
                    appendInfoPrefix()
                    info("Nachricht: ")
                    appendNewline()
                    append {
                        spacer(error.errorMessage.take(50))
                        if (error.errorMessage.length > 50) {
                            spacer("... (gekürzt)")
                        }
                        hoverEvent(buildText {
                            spacer(error.errorMessage)
                        })
                        clickCopiesToClipboard(error.errorMessage)
                    }

                    appendNewline()
                    appendInfoPrefix()
                    info("Stacktrace: ")
                    appendNewline()
                    append {
                        spacer(error.stacktrace.take(75))
                        if (error.stacktrace.length > 75) {
                            appendNewline()
                            spacer("... (gekürzt, ${error.stacktrace.length} Zeichen total)")
                        }

                        hoverEvent(buildText {
                            spacer(error.stacktrace)
                        })
                        clickCopiesToClipboard(error.stacktrace)
                    }
                    appendNewline()
                    appendNewline()
                    darkSpacer("*" + "-".repeat(40) + "*")
                }
            }
        }
    }
}

private val pagination = Pagination<SurfCoreSystemError> {
    title { primary("System-Fehler Übersicht") }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                appendInfoPrefix()
                variableKey("Fehler-Uuid: ")
                variableValue(row.uuid.toString())
                appendSpace()
                spacer("(${row.occurrenceCount}x)")
                appendSpace()
                spacer("- ${row.getLocationClassName().take(50)}")
                if (row.getLocationClassName().length > 50) {
                    spacer("...")
                }
                clickRunsCommand("/systemerror view ${row.uuid}")
                hoverEvent(buildText {
                    spacer("Klicke, um Details zu diesem Fehler anzuzeigen.")
                    appendNewline()
                    spacer("Zuletzt: ${row.lastOccurred.format(dateTimeFormatter)}")
                })
            }
        )
    }
}
