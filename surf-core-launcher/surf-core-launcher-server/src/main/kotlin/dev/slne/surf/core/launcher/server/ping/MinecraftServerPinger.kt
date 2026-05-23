package dev.slne.surf.core.launcher.server.ping

import dev.slne.surf.core.api.common.server.state.ExternalSurfServerState
import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.CoreLauncherEnvironment
import dev.slne.surf.core.launcher.server.logger
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds

object MinecraftServerPinger {
    suspend fun build() {
        val process = CoreLauncher.serverProcess

        while (process.isAlive) {
            delay(5_000L.milliseconds)

            val reachable = pingServer(CoreLauncherEnvironment.SERVER_PORT)
            val currentStatus = CoreLauncher.getCurrentServerState()

            if (reachable) {
                if (currentStatus != ExternalSurfServerState.ONLINE) {
                    logger.atInfo().log("[Launcher] Server is ONLINE")
                }
            } else {
                if (currentStatus == ExternalSurfServerState.ONLINE) {
                    logger.atWarning().log("[Launcher] Server UNREACHABLE")
                }
            }
        }
    }

    private fun pingServer(port: Int): Boolean = try {
        Socket("127.0.0.1", port).use { true }
    } catch (e: IOException) {
        false
    }
}