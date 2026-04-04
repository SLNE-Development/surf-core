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
import dev.slne.surf.core.api.common.server.CommonSurfServer

open class SurfServerArgument(nodeName: String) :
    Argument<CommonSurfServer>(nodeName, StringArgumentType::string) {
    override fun getPrimitiveType(): Class<CommonSurfServer> {
        return CommonSurfServer::class.java
    }

    override fun getArgumentType(): CommandAPIArgumentType? {
        return CommandAPIArgumentType.PRIMITIVE_STRING
    }

    override fun <Source> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments,
    ): CommonSurfServer =
        SurfCoreApi.getCommonServerByName(StringArgumentType.getString(cmdCtx, key))
            ?: throw SimpleCommandExceptionType(
                VelocityBrigadierMessage.tooltip(
                    buildText {
                        appendErrorPrefix()
                        error("Der Server wurde nicht gefunden.")
                    }
                )
            ).create()
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