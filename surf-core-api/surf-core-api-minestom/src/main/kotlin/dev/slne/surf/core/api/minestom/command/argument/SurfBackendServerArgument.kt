package dev.slne.surf.core.api.minestom.command.argument

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPI
import dev.slne.minestom.lobby.api.command.commandapi.CommandAPICommand
import dev.slne.minestom.lobby.api.command.commandapi.CommandTree
import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.argument.CustomArgument
import dev.slne.minestom.lobby.api.command.commandapi.argument.StringArgument
import dev.slne.minestom.lobby.api.command.commandapi.suggestion.ArgumentSuggestions
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.SurfServer

class SurfBackendServerArgument(nodeName: String) :
    CustomArgument<SurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi
            .getServerByName(info.currentInput)
            .takeIf { it?.isBackend() == true }
            ?: throw CommandAPI.failWithMessage(
                buildText {
                    appendErrorPrefix()
                    error("Der Backend Server wurde nicht gefunden.")
                }
            )
    }) {

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SurfCoreApi
                    .getServers()
                    .filter { it.isBackend() }
                    .map { it.name }
            }
        )
    }
}

inline fun CommandTree.surfBackendServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SurfBackendServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.surfBackendServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SurfBackendServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.surfBackendServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SurfBackendServerArgument(nodeName).setOptional(optional).apply(block))