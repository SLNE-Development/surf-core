package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.MCCoroutineExceptionEvent
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.paper.surfServerConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException

object MCCoroutineExceptionListener : Listener {
    private val logger = Logger.getLogger(MCCoroutineExceptionListener::class.java.name)

    @EventHandler
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.exception is CancellationException) {
            return
        }

        event.isCancelled = true

        logger.log(Level.SEVERE, "MCCoroutine exception occurred", event.exception)

        try {
            runBlocking {
                launch {
                    try {
                        val surfCoreSystemError = surfCoreSystemErrorService.logError(
                            throwable = event.exception,
                            server = surfServerConfig.serverName
                        )
                        logger.info("This error has been logged with ID: ${surfCoreSystemError.id}")
                    } catch (e: Exception) {
                        logger.log(
                            Level.SEVERE,
                            "Failed to log MCCoroutine exception to database",
                            e
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error while handling MCCoroutine exception", e)
        }
    }
}
