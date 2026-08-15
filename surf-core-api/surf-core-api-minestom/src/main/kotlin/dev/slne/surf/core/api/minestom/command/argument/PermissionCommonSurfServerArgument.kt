package dev.slne.surf.core.api.minestom.command.argument

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPI
import dev.slne.minestom.lobby.api.command.commandapi.CommandAPICommand
import dev.slne.minestom.lobby.api.command.commandapi.CommandTree
import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.argument.CustomArgument
import dev.slne.minestom.lobby.api.command.commandapi.argument.StringArgument
import dev.slne.minestom.lobby.api.command.commandapi.suggestion.ArgumentSuggestions
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.hasPermission
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.minestom.util.permission

class PermissionSurfServerArgument(nodeName: String) :
    CustomArgument<CommonSurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi
            .getCommonServerByName(info.currentInput)
            ?.takeIf {
                info.sender.hasPermission(it.permission)
                        || info.sender.hasPermission("surf.core.server.*")
            } ?: throw CommandAPI.failWithMessage(
            buildText {
                appendErrorPrefix()
                error("Der Server wurde nicht gefunden.")
            }
        )
    }) {

    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection { sender ->
                SurfCoreApi.getCommonServers().filter {
                    sender.sender.hasPermission(it.permission) || sender.sender.hasPermission("surf.core.server.*")
                }.map { it.name }
            }
        )
    }
}

inline fun CommandTree.permissionSurfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    PermissionSurfServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.permissionSurfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    PermissionSurfServerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.permissionSurfServerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(PermissionSurfServerArgument(nodeName).setOptional(optional).apply(block))
