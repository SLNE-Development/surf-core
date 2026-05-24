package dev.slne.surf.core.launcher.server

import dev.slne.surf.api.standalone.SurfApiStandaloneBootstrap
import dev.slne.surf.core.api.common.server.state.ExternalSurfServerState
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.config.CoreLauncherConfig
import dev.slne.surf.core.launcher.server.ping.MinecraftServerPinger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

const val LOG_PREFIX = "\u001B[0;91m[CoreLauncher]\u001B[0m"

object CoreLauncher {
//    val redisApi = RedisApi.create("surf-core-launcher")
//    private val redisStatusMap =
//        redisApi.createSyncMap<String, ExternalSurfServerState>("server_status")


    var currentState: ExternalSurfServerState = ExternalSurfServerState.OFFLINE
        set(value) {
            field = value
            println("$LOG_PREFIX Server state changed to: $value")
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

    lateinit var serverProcess: Process


    suspend fun launch(args: Array<String>) {
        SurfApiStandaloneBootstrap.bootstrap()
        SurfApiStandaloneBootstrap.enable()

        withContext(Dispatchers.IO) {
            println("$LOG_PREFIX Connected to Redis!")
            updateServerState(ExternalSurfServerState.STARTING)

            serverProcess = ProcessBuilder(buildStartupCommand())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start()

            println("$LOG_PREFIX Server process started! (PID: ${serverProcess.pid()})")

            runCatching {
                MinecraftServerPinger.build()
            }
        }
    }


    fun shutdown() {
        println("$LOG_PREFIX Shutdown signal received, stopping server...")
        updateServerState(ExternalSurfServerState.STOPPING)

        serverProcess.destroy()

        if (serverProcess.isAlive) {
            serverProcess.waitFor(30, TimeUnit.SECONDS)
        }

        updateServerState(ExternalSurfServerState.OFFLINE)
//        redisApi.disconnect()

        if (serverProcess.isAlive) {
            println("$LOG_PREFIX Warning: Server process did not stop gracefully within 30s, waiting longer... (no force yet)")
            serverProcess.destroy()
        } else {
            println("$LOG_PREFIX Server process stopped gracefully.")
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