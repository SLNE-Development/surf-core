package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.MCCoroutineExceptionEvent
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.paper.surfServerConfig
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.coroutines.cancellation.CancellationException

object MCCoroutineExceptionListener : Listener {
    private val logger = logger()

    @EventHandler
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.exception is CancellationException) {
            return
        }

        event.isCancelled = true

        logger.atSevere()
            .log(
                "MCCoroutine exception occurred: ${event.exception.message}\n${event.exception.stackTraceToString()}"
            )


        runCatching {
            runBlocking {
                launch {
                    runCatching {
                        val surfCoreSystemError = surfCoreSystemErrorService.logError(
                            throwable = event.exception,
                            server = surfServerConfig.serverName
                        )
                        logger.atInfo()
                            .log("This error has been logged with Uuid: ${surfCoreSystemError.uuid}")
                    }.onFailure {
                        logger.atSevere().log(
                            "Failed to log MCCoroutine exception to database"
                        )
                    }
                }
            }
        }.onFailure {
            logger.atSevere().log("Error while handling MCCoroutine exception")
        }
    }
}
