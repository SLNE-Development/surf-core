package dev.slne.surf.core.minestom.command

import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.command.CommandRegistrar
import dev.slne.minestom.lobby.api.command.MinestomLampConfigVisitor
import dev.slne.minestom.lobby.api.command.argument.GameProfileArgument
import dev.slne.minestom.lobby.api.command.suggestion.PrefixFilteredSuggestionProvider
import dev.slne.minestom.lobby.api.command.suggestion.addPrefixFiltered
import dev.slne.minestom.lobby.api.command.suggestion.currentArgument
import dev.slne.minestom.lobby.api.player.LobbyPlayer
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.minestom.command.argument.PermissionSurfServer
import dev.slne.surf.core.api.minestom.command.argument.SurfOfflinePlayerArgument
import dev.slne.surf.core.core.common.permission.CorePermissions
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import revxrsal.commands.Lamp
import revxrsal.commands.LampBuilderVisitor
import revxrsal.commands.annotation.list.AnnotationList
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.minestom.MinestomLampConfig
import revxrsal.commands.minestom.actor.MinestomCommandActor
import revxrsal.commands.minestom.argument.ArgumentTypeFactory
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.node.ParameterNode
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream
import java.lang.reflect.Type
import java.util.*

private const val SUGGESTION_LIMIT = 500

@Singleton
class SurfCoreCommandTypeRegistrar :
    CommandRegistrar,
    MinestomLampConfigVisitor,
    LampBuilderVisitor<MinestomCommandActor> {

    override fun configure(builder: MinestomLampConfig.Builder<MinestomCommandActor>) {
        builder.argumentTypes { types ->
            types.addTypeFactory(SurfPlayerArgumentTypeFactory)
        }
    }

    override fun visit(builder: Lamp.Builder<MinestomCommandActor>) {
        builder.parameterTypes { types ->
            types.addParameterTypeFactory(CommonSurfServerParameterTypeFactory)
            types.addParameterType(SurfServer::class.java, surfServerType())
            types.addParameterType(SurfProxyServer::class.java, surfProxyServerType())
        }
    }

    override fun register(lamp: Lamp<MinestomCommandActor>) = Unit
}

private object SurfPlayerArgumentTypeFactory : ArgumentTypeFactory<MinestomCommandActor> {
    override fun getArgumentType(
        parameter: ParameterNode<MinestomCommandActor?, *>,
    ): Argument<*>? = when (parameter.type()) {
        SurfPlayer::class.java -> gameProfileArgument(
            parameter.name(),
            suggestions = { onlinePlayerNames() },
            resolver = ::findOnlinePlayer,
        )

        SurfOfflinePlayerArgument::class.java -> gameProfileArgument(
            parameter.name(),
            suggestions = { prefix -> offlinePlayerNames(prefix) },
            resolver = ::SurfOfflinePlayerArgument,
        )

        else -> null
    }
}

private object CommonSurfServerParameterTypeFactory :
    ParameterType.Factory<MinestomCommandActor> {

    override fun <T> create(
        parameterType: Type,
        annotations: AnnotationList,
        lamp: Lamp<MinestomCommandActor>,
    ): ParameterType<MinestomCommandActor, T>? {
        if (parameterType != CommonSurfServer::class.java) return null

        val permissionRestricted = annotations.get(PermissionSurfServer::class.java) != null
        @Suppress("UNCHECKED_CAST")
        return commonSurfServerType(permissionRestricted) as ParameterType<MinestomCommandActor, T>
    }
}

private fun <T : Any> gameProfileArgument(
    name: String,
    suggestions: (prefix: String) -> Collection<String>,
    resolver: (input: String) -> T?,
): Argument<T> = GameProfileArgument(name)
    .setSuggestionCallback { _, _, suggestion ->
        suggestion.addPrefixFiltered(
            suggestions(suggestion.currentArgument()),
            SUGGESTION_LIMIT,
        )
    }
    .map { _, input ->
        resolver(input) ?: throw ArgumentSyntaxException(
            "Der Spieler wurde nicht gefunden.",
            input,
            -1,
        )
    }

private fun findOnlinePlayer(input: String): SurfPlayer? = input
    .toUuidOrNull()
    ?.let(SurfCoreApi::getPlayer)
    ?: SurfCoreApi.getPlayer(input)

private fun onlinePlayerNames(): List<String> = SurfCoreApi
    .getOnlinePlayers()
    .mapNotNull(SurfPlayer::lastKnownName)

private fun offlinePlayerNames(prefix: String): List<String> {
    val online = onlinePlayerNames()
    val onlineNames = online.mapTo(ObjectOpenHashSet(online.size)) { it.lowercase() }
    return online + OfflinePlayerNameCache.findByPrefix(prefix)
        .filterNot { it.lowercase() in onlineNames }
}

private fun String.toUuidOrNull(): UUID? {
    if (length <= 16) return null // Shorter than a UUID, likely a username
    return runCatching(UUID::fromString).getOrNull()
}

private fun commonSurfServerType(permissionRestricted: Boolean) = surfParameterType(
    suggestions = { actor, _ ->
        SurfCoreApi.getCommonServers()
            .filter { !permissionRestricted || actor.canAccess(it) }
            .map(CommonSurfServer::name)
    },
    errorMessage = "Der Server wurde nicht gefunden.",
    resolver = { actor, input ->
        SurfCoreApi.getCommonServerByName(input)
            ?.takeIf { !permissionRestricted || actor.canAccess(it) }
    },
)

private fun surfServerType() = surfParameterType(
    suggestions = { _, _ -> SurfCoreApi.getServers().map(SurfServer::name) },
    errorMessage = "Der Backend-Server wurde nicht gefunden.",
    resolver = { _, input -> SurfCoreApi.getServerByName(input) },
)

private fun surfProxyServerType() = surfParameterType(
    suggestions = { _, _ -> SurfCoreApi.getProxies().map(SurfProxyServer::name) },
    errorMessage = "Der Proxy-Server wurde nicht gefunden.",
    resolver = { _, input -> SurfCoreApi.getProxyServerByName(input) },
)

private fun <T : Any> surfParameterType(
    suggestions: (MinestomCommandActor, String) -> Collection<String>,
    errorMessage: String,
    resolver: (MinestomCommandActor, String) -> T?,
) = object : ParameterType<MinestomCommandActor, T> {
    override fun parse(
        input: MutableStringStream,
        context: ExecutionContext<MinestomCommandActor>,
    ): T {
        val value = input.readString()
        return resolver(context.actor(), value) ?: throw CommandErrorException(errorMessage)
    }

    override fun defaultSuggestions() = PrefixFilteredSuggestionProvider(
        SUGGESTION_LIMIT,
    ) { context ->
        suggestions(context.actor(), context.currentArgument())
    }
}

private fun MinestomCommandActor.canAccess(server: CommonSurfServer): Boolean {
    val player = sender() as? LobbyPlayer ?: return true

    return player.hasPermission(CorePermissions.server(server.name)) ||
            player.hasPermission(CorePermissions.SERVER_WILDCARD)
}
