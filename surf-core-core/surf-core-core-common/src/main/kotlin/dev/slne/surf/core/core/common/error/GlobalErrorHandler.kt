package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.core.common.config.surfServerConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Global error handler for uncaught exceptions.
 * Logs errors to the database and prevents duplicate logging.
 */
object GlobalErrorHandler {
    private val logger = Logger.getLogger(GlobalErrorHandler::class.java.name)
    private val errorScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Installs the global uncaught exception handler for all threads.
     */
    fun install() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleError(thread.name, throwable)
        }
        logger.info("Global error handler installed")
    }
    
    /**
     * Creates a coroutine exception handler that logs errors to the database.
     */
    fun createCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { context, throwable ->
            handleError(context.toString(), throwable)
        }
    }
    
    /**
     * Handles an error by logging it to the database.
     */
    private fun handleError(source: String, throwable: Throwable) {
        try {
            logger.log(Level.SEVERE, "Uncaught exception from $source", throwable)
            
            // Log to database asynchronously
            errorScope.launch {
                try {
                    systemErrorService.logError(
                        throwable = throwable,
                        server = surfServerConfig.serverName
                    )
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Failed to log error to database", e)
                }
            }
        } catch (e: Exception) {
            // Fallback if error handling itself fails
            logger.log(Level.SEVERE, "Error handler failed", e)
        }
    }
    
    /**
     * Logs an error manually.
     */
    fun logError(throwable: Throwable) {
        handleError("Manual", throwable)
    }
}
