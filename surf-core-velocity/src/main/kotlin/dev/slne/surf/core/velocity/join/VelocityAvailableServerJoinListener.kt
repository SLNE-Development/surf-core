package dev.slne.surf.core.velocity.join

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.join.isOnlyAlreadyConnected
import dev.slne.surf.core.velocity.plugin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import java.util.*
import kotlin.time.Duration.Companion.seconds

object VelocityAvailableServerJoinListener : CoreJoinListener {
    override val name = "available-server"

    override suspend fun checkJoin(playerUuid: UUID): CoreJoinResult {
        val candidates = candidateServers(playerUuid)

        if (candidates.isEmpty()) {
            return CoreJoinResult.denied(Component.text("Es ist kein Server verfügbar"))
        }

        val results = coroutineScope {
            candidates.map { server ->
                async {
                    server to (withTimeoutOrNull(4.seconds) {
                        SurfCoreApi.checkJoin(playerUuid, server)
                    } ?: SurfJoinCheckResult.TIMEOUT)
                }
            }.awaitAll()
        }

        if (results.any { (_, result) -> result.isAllowed() || result.isOnlyAlreadyConnected() }) {
            return CoreJoinResult.allowed()
        }

        return CoreJoinResult.denied(
            Component.text("Es ist kein Server verfügbar: ").append(
                Component.join(
                    JoinConfiguration.commas(true),
                    results.map { (server, result) ->
                        Component.text("${server.name} (")
                            .append(
                                result.denials.firstOrNull()?.reason
                                    ?: Component.text(result.status.name)
                            )
                            .append(Component.text(")"))
                    }
                )
            )
        )
    }

    private fun candidateServers(playerUuid: UUID): List<SurfServer> {
        val names = linkedSetOf<String>()
        SurfCoreApi.getPlayer(playerUuid)?.currentServerName?.let(names::add)
        names += plugin.proxy.configuration.attemptConnectionOrder

        return names
            .filter { plugin.proxy.getServer(it).isPresent }
            .mapNotNull(SurfCoreApi::getServerByName)
    }
}
