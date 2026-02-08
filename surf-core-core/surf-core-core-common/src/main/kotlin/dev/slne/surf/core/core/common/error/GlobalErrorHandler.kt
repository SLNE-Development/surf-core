package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*

object GlobalErrorHandler {
    private val errorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val logger = logger()

    fun install() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleError(thread.name, throwable)
        }
        logger.atInfo().log("Global error handler installed")
    }

    private fun handleError(source: String, throwable: Throwable) {
        runCatching {
            logger.atSevere()
                .log("Uncaught exception from $source: ${throwable.message}\n${throwable.stackTraceToString()}")

            errorScope.launch {
                runCatching {
                    val surfCoreSystemError = surfCoreSystemErrorService.logError(
                        throwable = throwable,
                        server = SurfServer.current().name
                    )
                    logger.atInfo()
                        .log("This error has been logged with code: ${surfCoreSystemError.errorCode} (ID: ${surfCoreSystemError.uuid})")
                }.onFailure {
                    logger.atSevere().log("Failed to log error to database")
                }
            }
        }.onFailure {
            logger.atSevere().log("Error handler failed")
        }
    }

    fun logError(throwable: Throwable) {
        handleError("Manual", throwable)
    }

    fun shutdown() {
        runBlocking {
            runCatching {
                errorScope.coroutineContext.job.cancelAndJoin()
                logger.atInfo().log("Global error handler shutdown complete")
            }.onFailure {
                logger.atWarning().log("Error during error handler shutdown")
            }
        }
    }
}
