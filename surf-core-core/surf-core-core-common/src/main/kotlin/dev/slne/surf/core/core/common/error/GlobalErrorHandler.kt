package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.api.common.server.SurfServer
import kotlinx.coroutines.*
import java.util.logging.Level
import java.util.logging.Logger

object GlobalErrorHandler {
    private val logger = Logger.getLogger(GlobalErrorHandler::class.java.name)
    private val errorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun install() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleError(thread.name, throwable)
        }
        logger.info("Global error handler installed")
    }

    fun createCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { context, throwable ->
            handleError(context.toString(), throwable)
        }
    }

    private fun handleError(source: String, throwable: Throwable) {
        try {
            logger.log(Level.SEVERE, "Uncaught exception from $source", throwable)

            errorScope.launch {
                try {
                    val surfCoreSystemError = surfCoreSystemErrorService.logError(
                        throwable = throwable,
                        server = SurfServer.current().name
                    )
                    logger.info("This error has been logged with ID: ${surfCoreSystemError.id}")
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Failed to log error to database", e)
                }
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error handler failed", e)
        }
    }

    fun logError(throwable: Throwable) {
        handleError("Manual", throwable)
    }

    fun shutdown() {
        runBlocking {
            try {
                errorScope.coroutineContext.job.cancelAndJoin()
                logger.info("Global error handler shutdown complete")
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Error during error handler shutdown", e)
            }
        }
    }
}
