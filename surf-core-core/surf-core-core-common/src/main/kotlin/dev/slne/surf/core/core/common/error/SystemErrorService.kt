package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList

val systemErrorService = requiredService<SystemErrorService>()

/**
 * Service for managing system-wide errors.
 * This is separate from player-specific error logging.
 */
interface SystemErrorService {
    /**
     * Logs a system error from a throwable.
     * Automatically extracts stacktrace and location information.
     */
    suspend fun logError(
        throwable: Throwable,
        server: String
    ): SystemError {
        val message = throwable.message ?: throwable::class.java.simpleName
        val stacktrace = throwable.stackTraceToString()
        val location = extractLocation(throwable)
        
        return logError(message, stacktrace, location, server)
    }
    
    /**
     * Logs a system error with explicit parameters.
     */
    suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SystemError

    /**
     * Gets all system errors.
     */
    suspend fun getAllErrors(): ObjectList<SystemError>
    
    /**
     * Gets a specific error by ID.
     */
    suspend fun getError(id: Long): SystemError?

    /**
     * Extracts the relevant location from a throwable's stack trace.
     */
    fun extractLocation(throwable: Throwable): String {
        val stackTrace = throwable.stackTrace
        if (stackTrace.isEmpty()) {
            return "Unknown"
        }
        
        // Find the first stack trace element that's not from Java/Kotlin internals
        val relevantElement = stackTrace.firstOrNull { element ->
            !element.className.startsWith("java.") &&
            !element.className.startsWith("kotlin.") &&
            !element.className.startsWith("sun.") &&
            !element.className.startsWith("jdk.") &&
            !element.className.startsWith("org.jetbrains.exposed.") &&
            !element.className.startsWith("kotlinx.coroutines.")
        } ?: stackTrace.first()
        
        return "${relevantElement.className}.${relevantElement.methodName}:${relevantElement.lineNumber}"
    }
}
