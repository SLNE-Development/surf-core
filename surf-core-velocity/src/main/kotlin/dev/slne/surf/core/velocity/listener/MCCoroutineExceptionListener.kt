package dev.slne.surf.core.velocity.listener

import com.github.shynixn.mccoroutine.velocity.MCCoroutineExceptionEvent
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import dev.slne.surf.core.core.common.error.surfCoreSystemErrorService
import dev.slne.surf.core.velocity.surfServerConfig
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object MCCoroutineExceptionListener {
    private val logger = logger()

    @Subscribe
    fun onMCCoroutineException(event: MCCoroutineExceptionEvent) {
        event.result = ResultedEvent.GenericResult.denied()

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
