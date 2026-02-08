package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.MCCoroutineExceptionEvent
import dev.slne.surf.core.core.common.error.systemErrorService
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.core.paper.surfServerConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Listens to MCCoroutine exception events and logs them to the system error database.
 * This handles exceptions from coroutines managed by MCCoroutine.
 */
object MCCoroutineExceptionListener : Listener {
    private val logger = Logger.getLogger(MCCoroutineExceptionListener::class.java.name)

    @EventHandler
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        // Only handle exceptions from our plugin
        if (event.plugin != plugin) {
            return
        }
        
        // Skip CancellationException as per MCCoroutine documentation
        if (event.exception is CancellationException) {
            return
        }

        // Cancel the event to prevent MCCoroutine's default logging
        event.isCancelled = true

        // Log the exception
        logger.log(Level.SEVERE, "MCCoroutine exception occurred", event.exception)

        // Log to database asynchronously with try-catch to prevent infinite stacking
        try {
            runBlocking {
                launch {
                    try {
                        val systemError = systemErrorService.logError(
                            throwable = event.exception,
                            server = surfServerConfig.serverName
                        )
                        logger.info("This error has been logged with ID: ${systemError.id}")
                    } catch (e: Exception) {
                        // Log but don't rethrow to prevent infinite exception loop
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
