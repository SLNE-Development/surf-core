package dev.slne.surf.core.launcher.server

import dev.slne.surf.api.standalone.SurfApiStandaloneBootstrap
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.config.CoreLauncherConfig
import dev.slne.surf.core.launcher.server.ping.MinecraftServerPinger
import dev.slne.surf.core.launcher.server.updater.process.PluginUpdater
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

private val secondDateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

val LOG_PREFIX
    get() =
        "\u001B[0;91m[${
            LocalDateTime.now().format(secondDateTimeFormatter)
        } CoreLauncher]\u001B[0m"

object CoreLauncher {
    private val shuttingDown = AtomicBoolean(false)
    lateinit var serverProcess: Process
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    var minecraftServerOnline: Boolean = false
    val config by lazy {
        CoreLauncherConfig.getConfig()
    }

    suspend fun launch() {
        SurfApiStandaloneBootstrap.bootstrap()
        SurfApiStandaloneBootstrap.enable()

        if (config.autoUpdateSurfPlugins) {
            println("$LOG_PREFIX Searching plugin updates...")

            withTimeoutOrNull(20.seconds) {
                if (config.personalAccessToken.isBlank()) {
                    println("$LOG_PREFIX No GitHub personal access token provided, skipping plugin update check")
                    return@withTimeoutOrNull
                }

                PluginUpdater.start()
            }
                ?: println("$LOG_PREFIX Plugin update check timed out after 30 seconds, continuing with server startup")
        }

        println("$LOG_PREFIX Starting Minecraft Server...")

        val command = buildStartupCommand()

        serverProcess = ProcessBuilder(command)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        println("$LOG_PREFIX Server process started")

        monitorJob = scope.launch {
            launch {
                serverProcess.inputStream.bufferedReader().forEachLine { line ->
                    println(line)

                    if (line.contains(
                            config.startedMessage,
                            ignoreCase = true
                        )
                    ) {
                        minecraftServerOnline = true
                        println("$LOG_PREFIX Server is now online.")
                    }
                }
            }

            MinecraftServerPinger.monitor(serverProcess)
        }
    }

    suspend fun shutdown() {
        println("$LOG_PREFIX Shutting down launcher/server...")

        monitorJob?.cancelAndJoin()

        if (::serverProcess.isInitialized && serverProcess.isAlive) {
            serverProcess.destroy()

            if (!serverProcess.waitFor(45, TimeUnit.SECONDS)) {
                println("$LOG_PREFIX Server did not stop within 45 seconds")
            }
        }

        scope.cancel()
    }

    fun isShuttingDown(): Boolean = shuttingDown.get()

    private fun buildStartupCommand(): List<String> {
        val base = config.serverStartupCommand

        val parts = Regex("""[^\s"]+|"([^"]*)"""")
            .findAll(base)
            .map { it.value.replace("\"", "") }
            .toMutableList()

        val flag = "-D${LauncherConstants.PROPERTY_LAUNCHED_BY_CORE}"

        if (parts.none { it == flag }) {
            parts.add(1, flag)
        }

        return parts
    }
}

suspend fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            CoreLauncher.shutdown()
            SurfApiStandaloneBootstrap.shutdown()
        }
    })

    CoreLauncher.launch()
    CoreLauncher.serverProcess.waitFor()
    SurfApiStandaloneBootstrap.shutdown()
}