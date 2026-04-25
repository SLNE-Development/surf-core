package dev.slne.surf.core.velocity.command

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.ConsoleCommandSource
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.velocity.command.argument.surfServerArgument
import dev.slne.surf.core.core.common.server.SurfServerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.velocity.permission.PermissionList
import dev.slne.surf.core.velocity.plugin
import net.kyori.adventure.text.event.ClickEvent
import java.time.Duration
import java.time.OffsetDateTime

fun coreServiceCommand() = subcommand("service") {
    withPermission(PermissionList.CORE_COMMAND_SERVICE)

    literalArgument("list") {
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
                    spacer("-".repeat(30 - service.name.length / 2))
                    variableValue(service.name)
                    spacer("-".repeat(30 - service.name.length / 2))
                    spacer("*")

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
                        Duration.between(service.startedAt, OffsetDateTime.now()).toString()
                    )

                    appendNewline()
                    appendCorePrefix()
                    spacer("*")
                    spacer("-".repeat(30))
                    spacer("*")
                }
            }
        }


        literalArgument("sudo") {
            textArgument("command")

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

private val blockedCommands =
    listOf("lp", "lpv", "luckperms", "perm", "permission", "permissions", "luckpermsvelocity")