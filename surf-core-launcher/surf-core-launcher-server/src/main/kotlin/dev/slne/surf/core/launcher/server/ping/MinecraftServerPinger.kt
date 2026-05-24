package dev.slne.surf.core.launcher.server.ping

import dev.slne.surf.core.api.common.server.state.ExternalSurfServerState
import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.CoreLauncherEnvironment
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import kotlinx.coroutines.*
import java.io.IOException
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds

object MinecraftServerPinger {
    suspend fun build() {
        val process = CoreLauncher.serverProcess

        coroutineScope {
            launch {
                process.waitFor()

                delay(500.milliseconds)

                val lastStatus = CoreLauncher.getCurrentServerState()
                if (lastStatus != ExternalSurfServerState.OFFLINE && lastStatus != ExternalSurfServerState.STOPPING) {
                    println("$LOG_PREFIX Server process died unexpectedly → CRASHED")
                    CoreLauncher.updateServerState(ExternalSurfServerState.CRASHED)
                }

                this@coroutineScope.cancel()
            }

            launch {
                while (process.isAlive) {
                    delay(5_000L.milliseconds)

                    val reachable = pingServer(CoreLauncherEnvironment.SERVER_PORT)
                    val currentStatus = CoreLauncher.getCurrentServerState()

                    if (reachable) {
                        if (currentStatus != ExternalSurfServerState.ONLINE) {
                            println("$LOG_PREFIX Server is ONLINE")
                            CoreLauncher.updateServerState(ExternalSurfServerState.ONLINE)
                        }
                    } else {
                        if (currentStatus == ExternalSurfServerState.ONLINE) {
                            println("$LOG_PREFIX Server UNREACHABLE")
                            CoreLauncher.updateServerState(ExternalSurfServerState.UNREACHABLE)
                        }
                    }
                }
            }

            awaitCancellation()
        }
    }

    private fun pingServer(port: Int): Boolean = try {
        Socket("127.0.0.1", port).use { true }
    } catch (e: IOException) {
        false
    }
}