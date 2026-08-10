package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.paper.util.permission

class PermissionSurfServerArgument(nodeName: String) :
    CustomArgument<CommonSurfServer, String>(StringArgument(nodeName), { info ->
        SurfCoreApi.getCommonServerByName(info.input)
            ?.takeIf {
                info.sender.hasPermission(it.permission) || info.sender.hasPermission(
                    "surf.core.server.*"
                )
            }
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Der Server wurde nicht gefunden.")
                })
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
