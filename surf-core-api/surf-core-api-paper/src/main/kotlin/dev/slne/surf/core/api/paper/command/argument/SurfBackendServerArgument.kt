package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.SurfServer

class SurfBackendServerArgument(nodeName: String) :
    CustomArgument<SurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getServerByName(info.input).takeIf { it?.isBackend() == true }
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Der Backend Server wurde nicht gefunden.")
                })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SurfCoreApi.getServers().filter { it.isBackend() }.map { it.name }
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