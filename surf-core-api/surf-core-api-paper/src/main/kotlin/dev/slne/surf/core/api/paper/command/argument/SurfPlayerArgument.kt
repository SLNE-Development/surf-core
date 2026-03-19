package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

class SurfPlayerArgument(nodeName: String) :
    CustomArgument<SurfPlayer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getPlayer(info.input) ?: throw CustomArgumentException.fromAdventureComponent(
            buildText {
                appendErrorPrefix()
                error("Der Spieler wurde nicht gefunden.")
            })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SurfCoreApi.getOnlinePlayers().mapNotNull { it.lastKnownName }
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