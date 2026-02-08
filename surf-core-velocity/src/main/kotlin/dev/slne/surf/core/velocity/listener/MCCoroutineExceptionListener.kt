package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.MCCoroutineExceptionEvent
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.velocity.surfServerConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.cancellation.CancellationException

object MCCoroutineExceptionListener {
    private val logger = Logger.getLogger(MCCoroutineExceptionListener::class.java.name)

    @Subscribe
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.exception is CancellationException) {
            return
        }
        event.result = ResultedEvent.GenericResult.denied()

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
