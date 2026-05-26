package dev.slne.surf.core.launcher.server.ping

import dev.slne.surf.core.api.common.server.state.SurfServiceStatus
import dev.slne.surf.core.launcher.api.redis.ServiceStatusRedisEvent
import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.CoreLauncherEnvironment
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

object MinecraftServerPinger {
    suspend fun monitor(process: Process): Unit = coroutineScope {
        launch {
            val exitCode = process.waitFor()
            if (CoreLauncher.isShuttingDown()) {
                return@launch
            }

            if (exitCode == 0) {
                println("$LOG_PREFIX Minecraft server stopped")
            } else {
                println("$LOG_PREFIX Minecraft server crashed!")
                CoreLauncher.redisApi.publishEvent(
                    ServiceStatusRedisEvent(
                        serviceName = CoreLauncher.config.serverName,
                        status = SurfServiceStatus.CRASHED
                    )
                )
            }
        }

        launch {
            CoreLauncher.serverOnline.first { it }

            if (!process.isAlive) {
                return@launch
            }

            while (process.isAlive && !CoreLauncher.isShuttingDown()) {
                delay(3.seconds)

                val reachable = withContext(Dispatchers.IO) {
                    isMinecraftReady(CoreLauncherEnvironment.SERVER_PORT)
                }

                if (!reachable) {
                    println("$LOG_PREFIX Minecraft server is unreachable")
                    CoreLauncher.redisApi.publishEvent(
                        ServiceStatusRedisEvent(
                            serviceName = CoreLauncher.config.serverName,
                            status = SurfServiceStatus.UNREACHABLE
                        )
                    )
                }
            }
        }

        awaitCancellation()
    }

    private fun isMinecraftReady(port: Int): Boolean =
        runCatching {
            Socket().use { socket ->
                val host = "127.0.0.1"
                socket.connect(InetSocketAddress(host, port), 1500)
                socket.soTimeout = 1500

                DataOutputStream(socket.getOutputStream()).use { output ->
                    DataInputStream(socket.getInputStream()).use { input ->
                        output.write(buildHandshakePacket(host, port))
                        output.write(byteArrayOf(0x01, 0x00)) // Status request
                        output.flush()

                        readVarInt(input)          // Packet length
                        val packetId = readVarInt(input) // Packet ID

                        packetId == 0x00           // 0x00 = gültiger Status Response
                    }
                }
            }
        }.getOrDefault(false)

    private fun readVarInt(input: DataInputStream): Int {
        var value = 0
        var position = 0
        while (true) {
            val byte = input.readByte().toInt()
            value = value or ((byte and 0x7F) shl position)
            if (byte and 0x80 == 0) break
            position += 7
            if (position >= 35) error("VarInt too large")
        }
        return value
    }

}