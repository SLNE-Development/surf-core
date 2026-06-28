package dev.slne.surf.core.api.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.CorePlayerStatusAccess
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.future

private const val SUGGESTION_LIMIT = 500

class SurfOfflinePlayerArgument(nodeName: String) :
    CustomArgument<Deferred<SurfPlayer?>, String>(StringArgument(nodeName), { info ->
        scope.future {
            SurfCoreApi.getOfflinePlayer(info.input)
        }.asDeferred()
    }) {
    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollectionAsync { viewerInfo ->
                scope.future {
                    val input = viewerInfo.currentArg ?: ""

                    val onlinePlayerNames = SurfCoreApi.getOnlinePlayers()
                        .filter { CorePlayerStatusAccess.hasAccess(viewerInfo.sender, it) }
                        .mapNotNull { it.lastKnownName }
                        .filter { input.isEmpty() || it.startsWith(input, ignoreCase = true) }

                    val onlineSet = onlinePlayerNames.toHashSet()
                    val offlinePlayerNames = OfflinePlayerNameCache.findByPrefix(input)
                        .filter { it !in onlineSet }

                    (onlinePlayerNames + offlinePlayerNames).take(SUGGESTION_LIMIT)
                }
            }
        )
    }

    companion object {
        private val log = logger()
        private val scope =
            CoroutineScope(Dispatchers.IO + CoroutineName("SurfOfflinePlayerArgument") + CoroutineExceptionHandler { _, throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("An error occurred in SurfOfflinePlayerArgument")
            })
    }
}

inline fun CommandTree.surfOfflinePlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SurfOfflinePlayerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.surfOfflinePlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SurfOfflinePlayerArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.surfOfflinePlayerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SurfOfflinePlayerArgument(nodeName).setOptional(optional).apply(block))