package dev.slne.surf.core.api.velocity.command.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer

open class SurfPlayerArgument(nodeName: String) :
    Argument<SurfPlayer>(nodeName, StringArgumentType::string) {
    override fun getPrimitiveType(): Class<SurfPlayer> {
        return SurfPlayer::class.java
    }

    override fun getArgumentType(): CommandAPIArgumentType? {
        return CommandAPIArgumentType.PRIMITIVE_STRING
    }

    override fun <Source> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments,
    ): SurfPlayer = SurfCoreApi.getPlayer(StringArgumentType.getString(cmdCtx, key))
        ?: throw SimpleCommandExceptionType(
            VelocityBrigadierMessage.tooltip(
                buildText {
                    appendErrorPrefix()
                    error("Der Spieler wurde nicht gefunden.")
                }
            )
        ).create()
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