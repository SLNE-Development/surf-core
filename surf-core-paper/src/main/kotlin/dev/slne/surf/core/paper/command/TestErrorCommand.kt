package dev.slne.surf.core.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.core.core.common.error.GlobalErrorHandler
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText


fun testErrorCommand() = commandTree("testerror") {
    withPermission(PermissionRegistry.COMMAND_CORE_ERROR)

    literalArgument("thread") {
        anyExecutor { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Triggering thread exception...")
            }

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

            // This will be caught by MCCoroutineExceptionListener
            throw RuntimeException("Test coroutine exception from /testerror coroutine")
        }
    }

    literalArgument("launch") {
        anyExecutor { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Triggering exception in launch...")
            }

            plugin.launch {
                throw RuntimeException("Test exception from plugin.launch in /testerror launch")
            }
        }
    }

    literalArgument("manual") {
        anyExecutor { executor, _ ->
            executor.sendText {
                appendSuccessPrefix()
                success("Logging manual error...")
            }

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
