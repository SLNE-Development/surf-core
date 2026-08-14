package dev.slne.surf.core.api.minestom.command.argument

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPICommand
import dev.slne.minestom.lobby.api.command.commandapi.CommandTree
import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.argument.CustomArgument
import dev.slne.minestom.lobby.api.command.commandapi.argument.StringArgument
import dev.slne.minestom.lobby.api.command.commandapi.suggestion.ArgumentSuggestions
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.*

private const val SUGGESTION_LIMIT = 500

class SurfOfflinePlayerArgument(nodeName: String) :
    CustomArgument<Deferred<SurfPlayer?>, String>(StringArgument(nodeName), { info ->
        scope.async {
            SurfCoreApi.getOfflinePlayer(info.currentInput)
        }
    }) {

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollection { viewerInfo ->
                val input = viewerInfo.currentArg

                val onlinePlayerNames = SurfCoreApi
                    .getOnlinePlayers()
                    .mapNotNull { it.lastKnownName }
                    .filter { input.isEmpty() || it.startsWith(input, ignoreCase = true) }

                val onlineSet =
                    onlinePlayerNames.mapTo(ObjectOpenHashSet(onlinePlayerNames.size)) { it.lowercase() }
                val offlinePlayerNames = OfflinePlayerNameCache.findByPrefix(input)
                    .filterNot { it.lowercase() in onlineSet }

                (onlinePlayerNames + offlinePlayerNames).take(SUGGESTION_LIMIT)
            }
        )
    }

    companion object {
        private val log = logger()
        private val scope =
            CoroutineScope(Dispatchers.Default + CoroutineName("SurfOfflinePlayerArgument") + CoroutineExceptionHandler { _, throwable ->
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