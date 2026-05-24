package dev.slne.surf.core.launcher.server

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.standalone.SurfApiStandaloneBootstrap
import dev.slne.surf.core.api.common.server.state.ExternalSurfServerState
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.config.CoreLauncherConfig
import dev.slne.surf.core.launcher.server.ping.MinecraftServerPinger
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

val logger = logger()

object CoreLauncher {
//    val redisApi = RedisApi.create("surf-core-launcher")
//    private val redisStatusMap =
//        redisApi.createSyncMap<String, ExternalSurfServerState>("server_status")


    var currentState: ExternalSurfServerState = ExternalSurfServerState.OFFLINE
        set(value) {
            field = value
            logger.atInfo().log("[CoreLauncher] Server state changed to: $value")
        }

    fun getCurrentServerState(): ExternalSurfServerState =
        currentState

    fun updateServerState(newState: ExternalSurfServerState) {
        currentState = newState
    }

    private fun buildStartupCommand(): List<String> {
        val base = CoreLauncherConfig.getConfig().serverStartupCommand
        val parts = base.split(" ").toMutableList()

        if (parts.none { it == "-D${LauncherConstants.PROPERTY_LAUNCHED_BY_CORE}=true" }) {
            parts.add(1, "-D${LauncherConstants.PROPERTY_LAUNCHED_BY_CORE}=true")
        }

        return parts
    }


    val serverProcess: Process = ProcessBuilder(
        buildStartupCommand()
    )
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()


    suspend fun launch(args: Array<String>) {
        SurfApiStandaloneBootstrap.bootstrap()
        SurfApiStandaloneBootstrap.enable()

        withContext(Dispatchers.IO) {
//            redisApi.freezeAndConnect()
            logger.atInfo().log("[CoreLauncher] Connected to Redis!")

            updateServerState(ExternalSurfServerState.STARTING)
        }

        MinecraftServerPinger.build()

        logger.atInfo().log("[CoreLauncher] Server process started! (PID: ${serverProcess.pid()})")

        coroutineScope {
            launch {
                awaitCancellation()
            }.join()
        }
    }


    fun shutdown() {
        logger.atInfo().log("[CoreLauncher] Shutdown signal received, stopping server...")
        updateServerState(ExternalSurfServerState.STOPPING)

        serverProcess.destroy()

        if (serverProcess.isAlive) {
            serverProcess.waitFor(30, TimeUnit.SECONDS)
        }

        updateServerState(ExternalSurfServerState.OFFLINE)
//        redisApi.disconnect()

        if (serverProcess.isAlive) {
            logger.atWarning()
                .log("[CoreLauncher] Server process did not stop gracefully within 30s, waiting longer... (no force yet)")
            serverProcess.destroy()
        } else {
            logger.atInfo().log("[CoreLauncher] Server process stopped gracefully.")
        }
    }
}

suspend fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            CoreLauncher.shutdown()
            SurfApiStandaloneBootstrap.shutdown()
        }
    })

    CoreLauncher.launch(args)
}