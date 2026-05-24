package dev.slne.surf.core.launcher.server.ping

import dev.slne.surf.core.launcher.server.CoreLauncher
import dev.slne.surf.core.launcher.server.CoreLauncherEnvironment
import dev.slne.surf.core.launcher.server.LOG_PREFIX
import kotlinx.coroutines.*
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
            }
        }

        launch {
            while (process.isAlive && CoreLauncher.minecraftServerOnline) {
                delay(5.seconds)

                val reachable = withContext(Dispatchers.IO) {
                    isMinecraftReady(CoreLauncherEnvironment.SERVER_PORT)
                }

                if (!reachable) {
                    println("$LOG_PREFIX Minecraft server is unreachable")
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
                        val handshake = buildHandshakePacket(host, port)

                        output.write(handshake)
                        output.write(byteArrayOf(0x01, 0x00))
                        output.flush()

                        input.readByte()

                        true
                    }
                }
            }
        }.getOrDefault(false)
}