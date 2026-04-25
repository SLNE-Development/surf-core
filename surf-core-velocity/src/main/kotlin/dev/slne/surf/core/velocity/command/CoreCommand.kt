package dev.slne.surf.core.velocity.command

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.ConsoleCommandSource
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.api.velocity.command.argument.surfPlayerArgument
import dev.slne.surf.core.api.velocity.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.niceRed
import dev.slne.surf.core.velocity.permission.PermissionList
import dev.slne.surf.core.velocity.plugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.jvm.optionals.getOrNull

fun coreCommand() = commandTree("core") {
    withPermission(PermissionList.CORE_COMMAND)

    anyExecutor { source, _ ->
        val coreVersion =
            plugin.pluginManager.getPlugin("surf-core-velocity")
                .getOrNull()?.description?.version?.getOrNull() ?: "Unbekannt"
        val platform = plugin.proxy.version.name
        val platformVersion = plugin.proxy.version.version
        val vendor = plugin.proxy.version.vendor

        source.sendText {
            appendCorePrefix()
            info("This proxy is running ")
            variableValue("surf-core-velocity")
            info(" version ")
            variableValue(coreVersion)
            info(" on ")
            variableValue(platform)
            appendSpace()
            variableValue(platformVersion)
            info(" by ")
            variableValue(vendor)
            info(".")
        }
    }

    literalArgument("player") {
        withPermission(PermissionList.CORE_COMMAND_PLAYER)

        literalArgument("#list") {
            anyExecutor { source, _ ->
                if (SurfPlayerService.players.isEmpty()) {
                    source.sendText {
                        appendCorePrefix()
                        error("Derzeit sind keine Spieler auf dem Netzwerk online.")
                    }
                    return@anyExecutor
                }

                source.sendText {
                    appendCorePrefix()
                    info("Derzeit sind ")
                    variableValue(SurfPlayerService.players.size)
                    info(" Spieler auf dem Netzwerk online: ")
                    appendCollection(SurfPlayerService.players) { player ->
                        buildText {
                            variableValue(player.username)
                            clickRunsCommand("/core player ${player.username} info")
                            hoverEvent(buildText {
                                info("Online auf ")
                                variableValue("${player.currentServer?.name ?: "Unbekannt"} (${player.currentProxy?.name ?: "Unbekannt"})")
                            })
                        }
                    }
                }
            }
        }

        surfPlayerArgument("target") {
            literalArgument("find") {
                anyExecutor { source, arguments ->
                    val target: SurfPlayer by arguments

                    source.sendText {
                        appendCorePrefix()
                        info("Der Spieler ")
                        variableValue(target.username)
                        info(" ist derzeit online auf ")
                        variableValue("${target.currentServer?.name ?: "Unbekannt"} (${target.currentProxy?.name ?: "Unbekannt"})")
                        info(". ")

                        append {
                            spacer("[TP]")
                            clickRunsCommand("/ntp ${target.username}")
                            hoverEvent(buildText {
                                info("Teleportiere dich zu ${target.username}")
                            })
                        }
                    }
                }
            }

            literalArgument("info") {
                anyExecutor { source, arguments ->
                    val target: SurfPlayer by arguments

                    source.sendText {
                        appendCorePrefix()
                        spacer("*")
                        spacer("-".repeat((30 - target.username.length) / 2))
                        variableValue(target.username)
                        spacer("-".repeat((30 - target.username.length) / 2))
                        spacer("*")

                        appendNewline()
                        appendCorePrefix()

                        appendNewline()
                        appendCorePrefix()
                        info("UUID: ")
                        variableValue(target.uuid.toString())

                        appendNewline()
                        appendCorePrefix()
                        info("Letzter Name: ")
                        variableValue(target.lastKnownName ?: "Unbekannt")

                        appendNewline()
                        appendCorePrefix()
                        info("Aktueller Server: ")
                        variableValue(target.currentServer?.name ?: "Unbekannt")

                        appendNewline()
                        appendCorePrefix()
                        info("Aktueller Proxy: ")
                        variableValue(target.currentProxy?.name ?: "Unbekannt")

                        appendNewline()
                        appendCorePrefix()
                        info("Erstmals gesehen: ")
                        variableValue(target.firstSeen?.let {
                            dateTimeFormatter.format(it).toString()
                        } ?: "Unbekannt")

                        appendNewline()
                        appendCorePrefix()
                        info("Verbindung transferiert: ")
                        variableValue(target.transferred)

                        appendNewline()
                        appendCorePrefix()
                        info("Ip Adresse: ")
                        variableValue(target.lastKnownIpAddress?.toString() ?: "Unbekannt")

                        appendNewline()
                        appendCorePrefix()

                        appendNewline()
                        appendCorePrefix()
                        spacer("*")
                        spacer("-".repeat(30))
                        spacer("*")
                    }
                }
            }

            literalArgument("notify") {
                textArgument("message") {
                    multiLiteralArgument(
                        "prefix",
                        "--moderation",
                        "--system",
                        "--warn",
                        optional = true
                    ) {
                        anyExecutor { source, arguments ->
                            val target: SurfPlayer by arguments
                            val message: String by arguments
                            val prefix: String? by arguments

                            target.sendText {
                                appendCorePrefix()
                                append(findPrefix(prefix))
                                append(miniMessage.deserialize(message))
                            }

                            source.sendText {
                                appendCorePrefix()
                                info("Die Nachricht wurde gesendet.")
                            }
                        }
                    }
                }
            }
        }
    }

    literalArgument("service") {
        withPermission(PermissionList.CORE_COMMAND_SERVICE)

        literalArgument("#list") {
            anyExecutor { source, _ ->
                val services = SurfServerService.servers

                if (services.isEmpty()) {
                    source.sendText {
                        appendCorePrefix()
                        error("Derzeit sind keine Server auf dem Netzwerk online.")
                    }
                    return@anyExecutor
                }

                source.sendText {
                    appendCorePrefix()
                    info("Derzeit sind ")
                    variableValue(services.size)
                    info(" Server aktiv: ")
                    appendCollection(services) { service ->
                        buildText {
                            variableValue(service.displayName)
                            clickRunsCommand("/core service ${service.name} info")
                            hoverEvent(buildText {
                                info("ID: ")
                                variableValue(service.uuid.toString())
                                appendNewline()
                                info("Spieler: ")
                                variableValue("${service.getPlayerCount()}/${service.maxPlayers}")
                            })
                        }
                    }
                }
            }
        }


        surfServerArgument("service") {
            literalArgument("shutdown") {
                textArgument("reason", optional = true) {
                    anyExecutorSuspend { source, arguments ->
                        val service: CommonSurfServer by arguments
                        val reason: String? by arguments

                        if (source is ConsoleCommandSource || service.getPlayerCount() <= 5) {
                            source.sendText {
                                appendInfoPrefix()
                                info("Der Server ")
                                variableValue(service.name)
                                info(" wird heruntergefahren...")
                            }

                            val result =
                                SurfServerService.shutdown(
                                    service,
                                    reason?.let { miniMessage.deserialize(it) })

                            if (result) {
                                source.sendText {
                                    appendCorePrefix()
                                    success("Der Server ")
                                    variableValue(service.name)
                                    success(" wurde erfolgreich heruntergefahren.")
                                }
                            } else {
                                source.sendText {
                                    appendCorePrefix()
                                    error("Der Server ")
                                    variableValue(service.name)
                                    error(" konnte nicht heruntergefahren werden.")
                                }
                            }

                            return@anyExecutorSuspend
                        }

                        source.sendText {
                            appendInfoPrefix()
                            info("Möchtest du wirklich den Server ")
                            variableValue(service.name)
                            info(" herunterfahren? Es sind derzeit ")
                            variableValue(service.getPlayerCount())
                            info(" Spieler online! ")
                            append {
                                spacer("[")
                                success("Bestätigen")
                                spacer("]")
                                clickEvent(ClickEvent.callback {
                                    plugin.pluginContainer.launch {
                                        source.sendText {
                                            appendInfoPrefix()
                                            info("Der Server ")
                                            variableValue(service.name)
                                            info(" wird heruntergefahren...")
                                        }

                                        val result =
                                            SurfServerService.shutdown(
                                                service,
                                                reason?.let { miniMessage.deserialize(it) })

                                        if (result) {
                                            source.sendText {
                                                appendCorePrefix()
                                                success("Der Server ")
                                                variableValue(service.name)
                                                success(" wurde erfolgreich heruntergefahren.")
                                            }
                                        } else {
                                            source.sendText {
                                                appendCorePrefix()
                                                error("Der Server ")
                                                variableValue(service.name)
                                                error(" konnte nicht heruntergefahren werden.")
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }

            literalArgument("info") {
                anyExecutor { source, arguments ->
                    val service: CommonSurfServer by arguments

                    source.sendText {
                        appendCorePrefix()
                        spacer("*")
                        spacer("-".repeat((30 - service.name.length) / 2))
                        variableValue(service.name)
                        spacer("-".repeat((30 - service.name.length) / 2))
                        spacer("*")

                        appendNewline()
                        appendCorePrefix()

                        appendNewline()
                        appendCorePrefix()
                        info("ID: ")
                        variableValue(service.uuid.toString())

                        appendNewline()
                        appendCorePrefix()
                        info("Interner Name: ")
                        variableValue(service.name)

                        appendNewline()
                        appendCorePrefix()
                        info("Anzeigename: ")
                        variableValue(service.displayName)

                        appendNewline()
                        appendCorePrefix()
                        info("Serverkategorie: ")
                        variableValue(service.category)

                        appendNewline()
                        appendCorePrefix()
                        info("Servertyp: ")
                        variableValue(if (service.isProxy()) "Proxy" else "Backend")

                        appendNewline()
                        appendCorePrefix()
                        info("Status: ")
                        variableValue(service.state.toString())

                        appendNewline()
                        appendCorePrefix()
                        info("Spieler: ")
                        variableValue("${service.getPlayerCount()}/${service.maxPlayers}")

                        appendNewline()
                        appendCorePrefix()
                        info("Uptime: ")
                        variableValue(
                            formatDuration(
                                Duration.between(
                                    service.startedAt,
                                    OffsetDateTime.now()
                                )
                            )
                        )

                        appendNewline()
                        appendCorePrefix()


                        appendNewline()
                        appendCorePrefix()
                        spacer("*")
                        spacer("-".repeat(30))
                        spacer("*")
                    }
                }
            }


            literalArgument("sudo") {
                textArgument("command") {
                    playerExecutorSuspend { player, arguments ->
                        val service: CommonSurfServer by arguments
                        val command: String by arguments

                        if (blockedCommands.any { command.startsWith(it, ignoreCase = true) }) {
                            player.sendText {
                                appendCorePrefix()
                                error("Du darfst diesen Befehl nicht auf dem Server ")
                                variableValue(service.name)
                                error(" ausführen.")
                            }
                            return@playerExecutorSuspend
                        }

                        val result = SurfServerService.executeCommand(service, command)

                        if (result) {
                            player.sendText {
                                appendCorePrefix()
                                success("Der Befehl wurde erfolgreich auf dem Server ")
                                variableValue(service.name)
                                success(" ausgeführt.")
                            }
                        } else {
                            player.sendText {
                                appendCorePrefix()
                                error("Der Befehl konnte nicht auf dem Server ")
                                variableValue(service.name)
                                error(" ausgeführt werden.")
                            }
                        }
                    }
                }
            }
        }
    }
}

private val blockedCommands =
    listOf("lp", "lpv", "luckperms", "perm", "permission", "permissions", "luckpermsvelocity")

private fun findPrefix(prefix: String?) = buildText {
    when (prefix) {
        "--moderation" -> {
            spacer("[")
            error("MODERATION")
            spacer("]")
            appendSpace()
        }

        "--system" -> {
            spacer("[")
            variableValue("SYSTEM")
            spacer("]")
            appendSpace()
        }

        "--warn" -> {
            spacer("[")
            niceRed("WARNUNG", TextDecoration.BOLD)
            spacer("]")
            appendSpace()
        }

        else -> Component.empty()
    }
}

private fun formatDuration(duration: Duration): String {
    val seconds = duration.seconds % 60
    val minutes = (duration.toMinutes() % 60)
    val hours = (duration.toHours() % 24)
    val days = duration.toDays()

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}