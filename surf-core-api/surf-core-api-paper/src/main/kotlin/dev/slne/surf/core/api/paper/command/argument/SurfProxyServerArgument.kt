package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.SurfProxyServer

class SurfProxyServerArgument(nodeName: String) :
    CustomArgument<SurfProxyServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getProxyServerByName(info.input)
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Der Proxy Server wurde nicht gefunden.")
                })
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