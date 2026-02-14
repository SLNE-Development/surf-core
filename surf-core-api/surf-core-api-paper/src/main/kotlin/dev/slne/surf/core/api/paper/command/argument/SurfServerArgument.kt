package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

class SurfServerArgument(nodeName: String) :
    CustomArgument<CommonSurfServer, String>(StringArgument(nodeName), { info ->
        surfCoreApi.getServerByName(info.input)
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Der Server wurde nicht gefunden.")
                })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                surfCoreApi.getServers().map { it.name }
            }
        )
    }
}

inline fun CommandTree.surfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SurfServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.surfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SurfServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.surfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SurfServerArgument(nodeName).setOptional(optional).apply(block))