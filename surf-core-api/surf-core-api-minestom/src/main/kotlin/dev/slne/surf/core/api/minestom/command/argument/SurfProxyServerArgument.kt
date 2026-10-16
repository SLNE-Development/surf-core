package dev.slne.surf.core.api.minestom.command.argument

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.SurfProxyServer

class SurfProxyServerArgument(nodeName: String) :
    CustomArgument<SurfProxyServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getProxyServerByName(info.currentInput)
            ?: CommandAPI.failWithMessage(
                buildText {
                    appendErrorPrefix()
                    error("Der Proxy Server wurde nicht gefunden.")
                }
            )
    }) {

    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SurfCoreApi.getProxies().map { it.name }
            }
        )
    }
}

inline fun CommandTree.surfProxyServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SurfProxyServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.surfProxyServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SurfProxyServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.surfProxyServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SurfProxyServerArgument(nodeName).setOptional(optional).apply(block))