package dev.slne.surf.core.minestom.command

import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.command.CommandRegistrar
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
import revxrsal.commands.Lamp
import revxrsal.commands.LampBuilderVisitor
import revxrsal.commands.annotation.list.AnnotationList
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.minestom.actor.MinestomCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream
import java.lang.reflect.Type

private const val SUGGESTION_LIMIT = 500

@Singleton
class SurfCoreCommandTypeRegistrar :
    CommandRegistrar,
    LampBuilderVisitor<MinestomCommandActor> {

    override fun visit(builder: Lamp.Builder<MinestomCommandActor>) {
        builder.parameterTypes { types ->
            types.addParameterType(SurfPlayer::class.java, surfPlayerType())
            types.addParameterType(SurfOfflinePlayerArgument::class.java, offlineSurfPlayerType())
            types.addParameterTypeFactory(CommonSurfServerParameterTypeFactory)
            types.addParameterType(SurfServer::class.java, surfServerType())
            types.addParameterType(SurfProxyServer::class.java, surfProxyServerType())
        }
    }

    override fun register(lamp: Lamp<MinestomCommandActor>) = Unit
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

private fun surfPlayerType() = surfParameterType(
    suggestions = { _, _ ->
        SurfCoreApi.getOnlinePlayers().mapNotNull(SurfPlayer::lastKnownName)
    },
    errorMessage = "Der Spieler wurde nicht gefunden.",
    resolver = { _, input -> SurfCoreApi.getPlayer(input) },
)

private fun offlineSurfPlayerType() = surfParameterType(
    suggestions = { _, prefix ->
        val online = SurfCoreApi.getOnlinePlayers().mapNotNull(SurfPlayer::lastKnownName)
        val onlineNames = online.toHashSet()
        online + OfflinePlayerNameCache.findByPrefix(prefix).filterNot(onlineNames::contains)
    },
    errorMessage = "Der Spieler wurde nicht gefunden.",
    resolver = { _, input -> SurfOfflinePlayerArgument(input) },
)

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

    override fun defaultSuggestions() = SuggestionProvider<MinestomCommandActor> { context ->
        val prefix = context.input().source().substringAfterLast(' ')
        suggestions(context.actor(), prefix)
            .asSequence()
            .distinct()
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .take(SUGGESTION_LIMIT)
            .toList()
    }
}

private fun MinestomCommandActor.canAccess(server: CommonSurfServer): Boolean {
    val player = sender() as? LobbyPlayer ?: return true
    return player.hasPermission(CorePermissions.server(server.name)) ||
            player.hasPermission(CorePermissions.SERVER_WILDCARD)
}
