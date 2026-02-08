package dev.slne.surf.core.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.core.common.error.GlobalErrorHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Test command to demonstrate the error logging system.
 * This should be removed or disabled in production.
 */
fun testErrorCommand() = commandTree("testerror") {
    withPermission(PermissionRegistry.COMMAND_CORE_ERROR)
    
    literalArgument("thread") {
        anyExecutorSuspend { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Triggering thread exception...")
            }
            
            // Trigger an exception in a new thread
            Thread {
                throw RuntimeException("Test thread exception from /testerror thread")
            }.start()
        }
    }
    
    literalArgument("coroutine") {
        anyExecutorSuspend { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Triggering coroutine exception...")
            }
            
            // Trigger an exception in a coroutine
            runBlocking {
                launch(GlobalErrorHandler.createCoroutineExceptionHandler()) {
                    throw RuntimeException("Test coroutine exception from /testerror coroutine")
                }
            }
        }
    }
    
    literalArgument("manual") {
        anyExecutorSuspend { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Logging manual error...")
            }
            
            // Manually log an error
            try {
                @Suppress("DIVISION_BY_ZERO")
                1 / 0
            } catch (e: Exception) {
                GlobalErrorHandler.logError(e)
            }
        }
    }
    
    literalArgument("duplicate") {
        anyExecutorSuspend { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Triggering duplicate errors...")
            }
            
            // Trigger the same error multiple times to test deduplication
            repeat(3) {
                Thread {
                    Thread.sleep(100L * it)
                    throw RuntimeException("Duplicate test error - this should only be logged once")
                }.start()
            }
        }
    }
}
