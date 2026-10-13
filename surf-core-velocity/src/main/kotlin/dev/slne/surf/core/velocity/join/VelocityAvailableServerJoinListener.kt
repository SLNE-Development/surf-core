package dev.slne.surf.core.velocity.join

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.core.common.join.isOnlyAlreadyConnected
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.velocity.plugin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

object VelocityAvailableServerJoinListener : CoreJoinListener {
    private val log = logger()

    override val name = "available-server"

    override suspend fun checkJoin(playerUuid: UUID): CoreJoinResult {
        val directServer = directServer(playerUuid)
        val fallbackServers = fallbackServers().filter { it.name != directServer?.name }
        val results = check(playerUuid, listOfNotNull(directServer) + fallbackServers)

        if (results.any { (_, result) -> result.isJoinable() }) {
            return CoreJoinResult.allowed()
        }

        if (results.isEmpty()) {
            return CoreJoinResult.denied(Component.text("Es ist kein Server verfügbar"))
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

    private suspend fun check(
        playerUuid: UUID,
        servers: List<SurfServer>
    ): List<Pair<SurfServer, SurfJoinCheckResult>> = coroutineScope {
        servers.map { server ->
            async {
                val result = try {
                    withTimeoutOrNull(4.seconds) {
                        SurfCoreApi.checkJoin(playerUuid, server)
                    } ?: SurfJoinCheckResult.TIMEOUT
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .log("Join check on %s failed for %s", server.name, playerUuid)
                    SurfJoinCheckResult.TARGET_UNREACHABLE
                }

                if (!result.isJoinable()) {
                    log.atInfo()
                        .log("Server %s not joinable for %s: %s", server.name, playerUuid, result.status)
                }

                server to result
            }
        }.awaitAll()
    }

    private fun SurfJoinCheckResult.isJoinable() = isAllowed() || isOnlyAlreadyConnected()

    private suspend fun directServer(playerUuid: UUID): SurfServer? {
        val player = SurfPlayerService.findPlayerByUuidRemote(playerUuid)
            ?: SurfCoreApi.getPlayer(playerUuid)
        val name = player?.currentServerName

        if (name == null) {
            log.atWarning()
                .log("No current server known for %s (player found: %s)", playerUuid, player != null)
            return null
        }

        if (plugin.proxy.getServer(name).isEmpty) {
            log.atWarning()
                .log("Current server '%s' of %s is not registered on this proxy", name, playerUuid)
            return null
        }

        val server = SurfCoreApi.getServerByName(name)
        if (server == null) {
            log.atWarning()
                .log("Current server '%s' of %s is not registered as surf server", name, playerUuid)
        }

        return server
    }

    private fun fallbackServers(): List<SurfServer> =
        plugin.proxy.configuration.attemptConnectionOrder
            .distinct()
            .filter { plugin.proxy.getServer(it).isPresent }
            .mapNotNull(SurfCoreApi::getServerByName)
}
