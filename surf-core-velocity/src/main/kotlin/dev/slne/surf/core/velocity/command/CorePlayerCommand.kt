package dev.slne.surf.core.velocity.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.core.api.velocity.command.argument.surfPlayerArgument
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import dev.slne.surf.core.core.common.util.niceRed
import dev.slne.surf.core.velocity.permission.PermissionList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

fun corePlayerCommand() = subcommand("player") {
    withPermission(PermissionList.CORE_COMMAND_PLAYER)

    literalArgument("list") {
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
                info(" Spieler auf dem Netzwerk online:")
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
                    spacer("-".repeat(30 - target.username.length / 2))
                    variableValue(target.username)
                    spacer("-".repeat(30 - target.username.length / 2))
                    spacer("*")

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
                    spacer("*")
                    spacer("-".repeat(30))
                    spacer("*")
                }
            }
        }

        literalArgument("notify") {
            textArgument("message")
            multiLiteralArgument("prefix", "--moderation", "--system", "--warn", optional = true)

            anyExecutor { source, arguments ->
                val target: SurfPlayer by arguments
                val message: String by arguments
                val prefix: String? by arguments

                target.sendText {
                    appendCorePrefix()
                    appendSpace()
                    append(findPrefix(prefix))
                    append(miniMessage.deserialize(message))
                }

                source.sendText {
                    appendCorePrefix()
                    info("Die Nachricht wurde an alle Spieler gesendet.")
                }
            }
        }
    }
}

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