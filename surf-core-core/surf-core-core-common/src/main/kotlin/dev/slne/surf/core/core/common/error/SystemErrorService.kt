package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList

val systemErrorService = requiredService<SystemErrorService>()

fun extractErrorLocation(throwable: Throwable): String {
    val stackTrace = throwable.stackTrace
    if (stackTrace.isEmpty()) {
        return "Unknown"
    }
    
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

interface SystemErrorService {
    suspend fun logError(
        throwable: Throwable,
        server: String
    ): SystemError {
        val message = throwable.message ?: throwable::class.java.simpleName
        val stacktrace = throwable.stackTraceToString()
        val location = extractErrorLocation(throwable)
        
        return logError(message, stacktrace, location, server)
    }
    
    suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SystemError

    suspend fun getAllErrors(): ObjectList<SystemError>
    
    suspend fun getError(id: Long): SystemError?
}
