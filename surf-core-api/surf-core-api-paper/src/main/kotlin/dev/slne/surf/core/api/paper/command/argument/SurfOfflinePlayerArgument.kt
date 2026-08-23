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
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.future
import java.util.TreeSet

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

                    val suggestions = ObjectArrayList<String>(SUGGESTION_LIMIT)
                    val onlineNames = TreeSet(String.CASE_INSENSITIVE_ORDER)

                    for (player in SurfCoreApi.getOnlinePlayers()) {
                        if (suggestions.size >= SUGGESTION_LIMIT) break
                        val name = player.lastKnownName ?: continue
                        if (input.isNotEmpty() && !name.startsWith(input, ignoreCase = true)) continue
                        if (!CorePlayerStatusAccess.hasAccess(viewerInfo.sender, player)) continue

                        suggestions += name
                        onlineNames += name
                    }

                    val remaining = SUGGESTION_LIMIT - suggestions.size
                    if (remaining > 0) {
                        for (name in OfflinePlayerNameCache.findByPrefix(
                            input,
                            remaining + onlineNames.size
                        )) {
                            if (suggestions.size >= SUGGESTION_LIMIT) break
                            if (name in onlineNames) continue

                            suggestions += name
                        }
                    }

                    suggestions
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