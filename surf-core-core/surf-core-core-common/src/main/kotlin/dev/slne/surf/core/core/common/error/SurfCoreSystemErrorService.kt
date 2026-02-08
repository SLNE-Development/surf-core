package dev.slne.surf.core.core.common.error

import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*

val surfCoreSystemErrorService = requiredService<SurfCoreSystemErrorService>()

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

interface SurfCoreSystemErrorService {
    suspend fun logError(
        throwable: Throwable,
        server: String
    ): SurfCoreSystemError {
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
    ): SurfCoreSystemError

    suspend fun getAllErrors(): ObjectList<SurfCoreSystemError>

    suspend fun getError(uuid: UUID): SurfCoreSystemError?
    
    suspend fun getError(code: String): SurfCoreSystemError?
    
    fun generateCode(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..10)
            .map { chars[dev.slne.surf.surfapi.core.api.util.random.nextInt(chars.length)] }
            .joinToString("")
    }
}
