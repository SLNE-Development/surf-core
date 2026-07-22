package dev.slne.surf.core.launcher.server

import dev.slne.surf.api.standalone.SurfApiStandaloneBootstrap
import dev.slne.surf.core.api.common.event.SurfServerStartEvent
import dev.slne.surf.core.api.common.event.redis.SurfEventFireRedisEvent
import dev.slne.surf.core.launcher.api.LauncherConstants
import dev.slne.surf.core.launcher.server.config.CoreLauncherConfig
import dev.slne.surf.core.launcher.server.ping.MinecraftServerPinger
import dev.slne.surf.core.launcher.server.updater.process.PluginUpdater
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.StandaloneRedisInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.Path
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
    private val redisInstance = StandaloneRedisInstance(
        name = "surf-core-launcher",
        configPath = findRedisPluginPath()
            ?: error("Could not find Redis plugin configuration path, cannot start Redis instance")
    )
    lateinit var redisApi: RedisApi

    val serverOnline = MutableStateFlow(false)

    private val startupErrorCounts = mutableMapOf<String, Int>()
    private val errorLineRegex = Regex("""\[[^]]*ERROR[^]]*]:\s*(?:\[([^]]+)])?""")

    val config by lazy {
        CoreLauncherConfig.getConfig()
    }

    suspend fun launch() = withContext(Dispatchers.IO) {
        startupErrorCounts.clear()

        SurfApiStandaloneBootstrap.bootstrap()
        SurfApiStandaloneBootstrap.enable()

        println("$LOG_PREFIX Initializing Redis instance...")

        redisInstance.create()
        redisApi = RedisApi.create()
        redisApi.freezeAndConnect()

        println("$LOG_PREFIX Redis instance initialized and connected")

        if (config.autoUpdateSurfPlugins) {
            println("$LOG_PREFIX (Updater) Searching for plugin updates")

            if (!PluginUpdater.hasConfiguredToken) {
                println(
                    "$LOG_PREFIX (Updater) No GitHub token configured in SURF_GITHUB_TOKEN " +
                            "or personalAccessToken; skipping plugin update check"
                )
            } else {
                try {
                    withTimeoutOrNull(20.seconds) {
                        PluginUpdater.start()
                        true
                    } ?: println(
                        "$LOG_PREFIX (Updater) Plugin update check timed out after 20 seconds; " +
                                "continuing with server startup"
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    println(
                        "$LOG_PREFIX (Updater) Plugin update check failed: " +
                                "${exception.message ?: exception::class.simpleName}; " +
                                "continuing with server startup"
                    )
                    if (config.logGithubReleaseFetchFailures) {
                        exception.printStackTrace()
                    }
                }
            }
        }

        println("$LOG_PREFIX Starting Minecraft Server...")

        val command = buildStartupCommand()

        serverProcess = ProcessBuilder(command)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        println("$LOG_PREFIX Server process started")

        redisApi.publishEvent(
            SurfEventFireRedisEvent(
                SurfServerStartEvent(
                    serverName = config.serverName
                )
            )
        )

        monitorJob = scope.launch {
            launch {
                serverProcess.inputStream.bufferedReader().forEachLine { line ->
                    println(line)

                    if (!serverOnline.value) {
                        extractStartupErrorPlugin(line)?.let { plugin ->
                            startupErrorCounts.merge(plugin, 1, Int::plus)
                        }
                    }

                    if (line.contains(
                            config.startedMessage,
                            ignoreCase = true
                        )
                    ) {
                        serverOnline.value = true
                        println("$LOG_PREFIX Server is now online.")
                        printStartupErrorReport()
                        launch {
                            try {
                                PluginUpdater.checkForCoreLauncherUpdate()
                            } catch (exception: CancellationException) {
                                throw exception
                            } catch (exception: Exception) {
                                println(
                                    "$LOG_PREFIX (GitHub) Core launcher update check failed: " +
                                            (exception.message ?: exception::class.simpleName.orEmpty())
                                )
                                if (config.logGithubReleaseFetchFailures) {
                                    exception.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }

            MinecraftServerPinger.monitor(serverProcess)
        }
    }

    suspend fun shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return
        }

        serverOnline.value = true

        println("$LOG_PREFIX Shutting down launcher/server...")
        println("$LOG_PREFIX Disconnecting Redis instance...")

        redisApi.disconnect()
        redisInstance.shutdown()
        println("$LOG_PREFIX Redis instance disconnected and shutdown")

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

    private fun findRedisPluginPath(): Path? {
        val possiblePaths = listOf(
            Path("plugins", "surf-redis-paper"),
            Path("plugins", "surf-redis-velocity")
        )

        return possiblePaths.firstOrNull { path ->
            path.toFile().exists()
        }
    }

    private val pluginMappings = mapOf(
        "ModernPluginLoadingStrategy" to "Plugin Loader"
    )

    private fun extractStartupErrorPlugin(line: String): String? {
        val match = errorLineRegex.find(line) ?: return null
        val pluginName = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }
        return pluginName?.let { pluginMappings[it] ?: it } ?: "Server"
    }

    private fun printStartupErrorReport() {
        if (startupErrorCounts.isEmpty()) {
            return
        }

        val total = startupErrorCounts.values.sum()
        println("$LOG_PREFIX Started with $total error(s):")

        startupErrorCounts.entries
            .sortedByDescending { it.value }
            .forEach { (plugin, count) ->
                println("$LOG_PREFIX   - $plugin: $count Errors")
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

    CoreLauncher.launch()
    CoreLauncher.serverProcess.waitFor()

    CoreLauncher.shutdown()
    SurfApiStandaloneBootstrap.shutdown()
}
