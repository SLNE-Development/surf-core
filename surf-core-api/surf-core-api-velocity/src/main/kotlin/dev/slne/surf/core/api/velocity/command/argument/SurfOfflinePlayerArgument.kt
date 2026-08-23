package dev.slne.surf.core.api.velocity.command.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.future

private const val SUGGESTION_LIMIT = 500

@Suppress("UNCHECKED_CAST")
open class SurfOfflinePlayerArgument(nodeName: String) :
    Argument<Deferred<SurfPlayer?>>(nodeName, StringArgumentType::string) {
    override fun getPrimitiveType(): Class<Deferred<SurfPlayer?>> {
        return Deferred::class.java as Class<Deferred<SurfPlayer?>>
    }

    override fun getArgumentType(): CommandAPIArgumentType? {
        return CommandAPIArgumentType.PRIMITIVE_STRING
    }

    override fun <Source> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments,
    ): Deferred<SurfPlayer?> = scope.future {
        SurfCoreApi.getOfflinePlayer(StringArgumentType.getString(cmdCtx, key))
    }.asDeferred()

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollectionAsync { viewerInfo ->
                scope.future {
                    val input = viewerInfo.currentArg ?: ""

                    val suggestions = ObjectArrayList<String>(SUGGESTION_LIMIT)
                    val onlineNames = ObjectOpenHashSet<String>()

                    for (player in SurfCoreApi.getOnlinePlayers()) {
                        if (suggestions.size >= SUGGESTION_LIMIT) break
                        val name = player.lastKnownName ?: continue
                        if (input.isNotEmpty() && !name.startsWith(input, ignoreCase = true)) continue

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