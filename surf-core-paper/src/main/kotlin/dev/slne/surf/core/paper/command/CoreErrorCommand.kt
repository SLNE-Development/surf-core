package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.arguments.MapArgumentBuilder
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.api.common.error.SurfCoreErrorFilter
import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.core.api.common.error.SurfCoreSystemErrorFilter
import dev.slne.surf.core.core.common.error.GlobalErrorHandler
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.core.common.player.surfCoreErrorLoggingService
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.clickRunsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import dev.slne.surf.surfapi.core.api.util.dateTimeFormatter
import net.kyori.adventure.text.format.TextDecoration
import java.time.OffsetDateTime
import java.util.*

fun coreErrorCommand() = commandTree("coreerror") {
    withPermission(PermissionRegistry.COMMAND_CORE_ERROR)

    literalArgument("player") {
        withPermission(PermissionRegistry.COMMAND_CORE_ERROR_PLAYER)
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
                        append(playerErrorPagination.renderComponent(error))
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
                        info("Zeitpunkt: ")
                        variableValue(error.timestamp.format(dateTimeFormatter))
                        appendNewline()
                        appendNewline()
                        darkSpacer("*" + "-".repeat(20) + "*")
                    }
                }
            }
        }
        literalArgument("list") {
            optionalArgument(
                MapArgumentBuilder<String, String>("query", ' ')
                    .withKeyMapper { it }
                    .withValueMapper { it }
                    .withKeyList(
                        listOf(
                            "--player",
                            "--code",
                            "--message",
                            "--server",
                            "--after",
                            "--before",
                            "--limit",
                            "--page"
                        )
                    )
                    .withoutValueList()
                    .build()
            ) {
                anyExecutorSuspend { executor, args ->
                    val query: Map<String, String>? by args

                    executor.sendText {
                        appendInfoPrefix()
                        info("Es wird nach Ergebnissen gesucht...")
                    }

                    val filter = query?.parsePlayerErrorFilters() ?: SurfCoreErrorFilter.empty()
                    val page = query?.get("--page")?.toIntOrNull() ?: 1

                    val errors = surfCoreErrorLoggingService.getErrors(filter)

                    if (errors.isEmpty()) {
                        executor.sendText {
                            appendErrorPrefix()
                            error("Es wurden keine Ergebnisse gefunden.")
                        }
                        return@anyExecutorSuspend
                    }

                    executor.sendText {
                        appendNewline()
                        append(playerErrorPagination.renderComponent(errors, page))
                    }
                }
            }
        }
    }

    literalArgument("system") {
        withPermission(PermissionRegistry.COMMAND_CORE_ERROR_SYSTEM)
        literalArgument("test") {
            literalArgument("thread") {
                anyExecutor { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Triggering thread exception...")
                    }

                    Thread {
                        error("Test thread exception from /testerror thread")
                    }.start()
                }
            }

            literalArgument("coroutine") {
                anyExecutorSuspend { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Triggering coroutine exception...")
                    }

                    error("Test coroutine exception from /testerror coroutine")
                }
            }

            literalArgument("launch") {
                anyExecutor { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Triggering exception in launch...")
                    }

                    plugin.launch {
                        error("Test exception from plugin.launch in /testerror launch")
                    }
                }
            }

            literalArgument("manual") {
                anyExecutor { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Logging manual error...")
                    }

                    try {
                        @Suppress("DIVISION_BY_ZERO")
                        1 / 0
                    } catch (e: Exception) {
                        GlobalErrorHandler.logError(e)
                    }
                }
            }

            literalArgument("customContext") {
                anyExecutor { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Triggering exception with custom context...")

                        plugin.launch(plugin.globalRegionDispatcher) {
                            error("Test exception with custom context from /testerror customContext")
                        }
                    }
                }
            }

            literalArgument("duplicate") {
                anyExecutorSuspend { executor, _ ->
                    executor.sendText {
                        appendSuccessPrefix()
                        success("Triggering duplicate errors...")
                    }

                    repeat(3) {
                        Thread {
                            Thread.sleep(100L * it)
                            throw RuntimeException("Duplicate test error - this should only be logged once")
                        }.start()
                    }
                }
            }
        }

        literalArgument("list") {
            optionalArgument(
                MapArgumentBuilder<String, String>("query", ' ')
                    .withKeyMapper { it }
                    .withValueMapper { it }
                    .withKeyList(
                        listOf(
                            "--uuid",
                            "--code",
                            "--message",
                            "--location",
                            "--server",
                            "--firstAfter",
                            "--firstBefore",
                            "--lastAfter",
                            "--lastBefore",
                            "--minCount",
                            "--limit",
                            "--page"
                        )
                    )
                    .withoutValueList()
                    .build()
            ) {
                anyExecutorSuspend { executor, args ->
                    val query: Map<String, String>? by args

                    executor.sendText {
                        appendInfoPrefix()
                        info("Es wird nach Ergebnissen gesucht...")
                    }

                    val filter =
                        query?.parseSystemErrorFilters() ?: SurfCoreSystemErrorFilter.empty()
                    val page = query?.get("--page")?.toIntOrNull() ?: 1

                    val errors = surfCoreSystemErrorService.getAllErrors(filter)

                    if (errors.isEmpty()) {
                        executor.sendText {
                            appendErrorPrefix()
                            error("Es wurden keine Ergebnisse gefunden.")
                        }
                        return@anyExecutorSuspend
                    }

                    executor.sendText {
                        appendNewline()
                        append(systemErrorPagination.renderComponent(errors, page))
                    }
                }
            }
        }
        literalArgument("viewCode") {
            stringArgument("code") {
                anyExecutorSuspend { executor, args ->
                    val code: String by args
                    val error = surfCoreSystemErrorService.getError(code) ?: run {
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
                        info("Fehlercode: ")
                        variableValue(error.errorCode)
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
                        info("Fehlercode: ")
                        variableValue(error.errorCode)
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
}

private val playerErrorPagination = Pagination<SurfCoreError> {
    title { primary("Fehler-Übersicht") }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                appendInfoPrefix()
                variableKey("Fehlercode: ")
                variableValue(row.code)
                appendSpace()
                spacer("(${row.timestamp.format(dateTimeFormatter)})")
                clickRunsCommand("/coreerror player viewCode ${row.code}")
                hoverEvent(buildText {
                    spacer("Klicke, um Details zu diesem Fehler anzuzeigen.")
                })
            }
        )
    }
}

private val systemErrorPagination = Pagination<SurfCoreSystemError> {
    title { primary("System-Fehler Übersicht") }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                appendInfoPrefix()
                variableKey("Fehlercode: ")
                variableValue(row.errorCode)
                appendSpace()
                spacer("(${row.occurrenceCount}x)")
                appendSpace()
                spacer("- ${row.getLocationClassName().take(50)}")
                if (row.getLocationClassName().length > 50) {
                    spacer("...")
                }
                clickRunsCommand("/coreerror system viewCode ${row.errorCode}")
                hoverEvent(buildText {
                    spacer("Klicke, um Details zu diesem Fehler anzuzeigen.")
                    appendNewline()
                    spacer("Zuletzt: ${row.lastOccurred.format(dateTimeFormatter)}")
                })
            }
        )
    }
}

private fun parseDateTime(input: String): OffsetDateTime? {
    return runCatching { OffsetDateTime.parse(input) }.getOrNull()
}

private suspend fun Map<String, String>.parsePlayerErrorFilters(): SurfCoreErrorFilter {
    val playerUuid = this["--player"]?.let { PlayerLookupService.getUuid(it) }

    return SurfCoreErrorFilter(
        playerUuid = playerUuid,
        code = this["--code"],
        messageLike = this["--message"],
        server = this["--server"],
        timestampAfter = this["--after"]?.let { parseDateTime(it) },
        timestampBefore = this["--before"]?.let { parseDateTime(it) },
        limit = this["--limit"]?.toIntOrNull() ?: 50
    )
}

private fun Map<String, String>.parseSystemErrorFilters(): SurfCoreSystemErrorFilter {
    return SurfCoreSystemErrorFilter(
        uuid = this["--uuid"]?.let { runCatching { UUID.fromString(it) }.getOrNull() },
        errorCode = this["--code"],
        messageLike = this["--message"],
        locationLike = this["--location"],
        server = this["--server"],
        firstOccurredAfter = this["--firstAfter"]?.let { parseDateTime(it) },
        firstOccurredBefore = this["--firstBefore"]?.let { parseDateTime(it) },
        lastOccurredAfter = this["--lastAfter"]?.let { parseDateTime(it) },
        lastOccurredBefore = this["--lastBefore"]?.let { parseDateTime(it) },
        minOccurrenceCount = this["--minCount"]?.toIntOrNull(),
        limit = this["--limit"]?.toIntOrNull() ?: 50
    )
}

