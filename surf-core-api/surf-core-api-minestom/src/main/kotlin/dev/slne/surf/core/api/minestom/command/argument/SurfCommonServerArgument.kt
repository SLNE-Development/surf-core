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
import dev.slne.surf.core.api.common.server.CommonSurfServer

class SurfServerArgument(nodeName: String) :
    CustomArgument<CommonSurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi
            .getCommonServerByName(info.currentInput)
            ?: CommandAPI.failWithMessage(
                buildText {
                    appendErrorPrefix()
                    error("Der Server wurde nicht gefunden.")
                }
            )
    }) {

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SurfCoreApi.getCommonServers().map { it.name }
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