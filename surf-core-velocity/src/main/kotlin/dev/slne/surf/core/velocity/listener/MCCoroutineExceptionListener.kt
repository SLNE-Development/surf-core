package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.MCCoroutineExceptionEvent
import com.velocitypowered.api.event.Subscribe
import dev.slne.surf.core.core.common.config.surfServerConfig
import dev.slne.surf.core.core.common.error.systemErrorService
import dev.slne.surf.core.velocity.plugin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Listens to MCCoroutine exception events and logs them to the system error database.
 * This handles exceptions from coroutines managed by MCCoroutine.
 */
object MCCoroutineExceptionListener {
    private val logger = Logger.getLogger(MCCoroutineExceptionListener::class.java.name)
    
    @Subscribe
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        // Only handle exceptions from our plugin
        if (event.plugin != plugin.pluginContainer) {
            return
        }
        
        // Skip CancellationException as per MCCoroutine documentation
        if (event.exception is CancellationException) {
            return
        }
        
        // Cancel the event to prevent MCCoroutine's default logging
        // since we're handling it ourselves
        event.isCancelled = true
        
        // Log the exception
        logger.log(Level.SEVERE, "MCCoroutine exception occurred", event.exception)
        
        // Log to database asynchronously with try-catch to prevent infinite stacking
        try {
            runBlocking {
                launch {
                    try {
                        systemErrorService.logError(
                            throwable = event.exception,
                            server = surfServerConfig.serverName
                        )
                    } catch (e: Exception) {
                        // Log but don't rethrow to prevent infinite exception loop
                        logger.log(Level.SEVERE, "Failed to log MCCoroutine exception to database", e)
                    }
                }
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error while handling MCCoroutine exception", e)
        }
    }
}
