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
import dev.slne.surf.core.api.common.player.SurfPlayer

class SurfPlayerArgument(nodeName: String) :
    CustomArgument<SurfPlayer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getPlayer(info.currentInput)
            ?: throw CommandAPI.failWithMessage(
                buildText {
                    appendErrorPrefix()
                    error("Der Spieler wurde nicht gefunden.")
                }
            )
    }) {

    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection { _ ->
                SurfCoreApi
                    .getOnlinePlayers()
                    .mapNotNull { it.lastKnownName }
            }
        )
    }
}

inline fun CommandTree.surfPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SurfPlayerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.surfPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SurfPlayerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.surfPlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SurfPlayerArgument(nodeName).setOptional(optional).apply(block))