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
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

open class SurfBackendServerArgument(nodeName: String) :
    Argument<CommonSurfServer>(nodeName, StringArgumentType.string()) {
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
    ): CommonSurfServer = surfCoreApi.getServerByName(StringArgumentType.getString(cmdCtx, key))
        .takeIf { it?.isBackend() == true }
        ?: throw SimpleCommandExceptionType(
            VelocityBrigadierMessage.tooltip(
                buildText {
                    appendErrorPrefix()
                    error("Der Backend Server wurde nicht gefunden.")
                }
            )
        ).create()
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