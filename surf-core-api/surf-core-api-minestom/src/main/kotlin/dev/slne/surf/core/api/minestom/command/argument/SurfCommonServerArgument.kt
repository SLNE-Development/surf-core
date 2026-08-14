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
import dev.slne.surf.core.api.common.server.CommonSurfServer

class SurfServerArgument(nodeName: String) :
    CustomArgument<CommonSurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi
            .getCommonServerByName(info.currentInput)
            ?: throw CommandAPI.failWithMessage(
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