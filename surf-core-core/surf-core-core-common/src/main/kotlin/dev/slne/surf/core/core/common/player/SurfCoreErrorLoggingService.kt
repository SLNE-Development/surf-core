package dev.slne.surf.core.core.common.player

import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.surfapi.core.api.util.random
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*

val surfCoreErrorLoggingService = requiredService<SurfCoreErrorLoggingService>()

interface SurfCoreErrorLoggingService {
    suspend fun logError(
        playerUuid: UUID,
        code: String,
        message: String,
        server: String,
        stacktrace: String,
        location: String
    ): SurfCoreError
    
    suspend fun logError(
        playerUuid: UUID,
        throwable: Throwable,
        server: String
    ): SurfCoreError {
        val code = generateCode()
        val message = throwable.message ?: throwable::class.java.simpleName
        val stacktrace = throwable.stackTraceToString()
        val location = extractLocation(throwable)
        
        return logError(playerUuid, code, message, server, stacktrace, location)
    }

    suspend fun getErrors(playerUuid: UUID): ObjectList<SurfCoreError>
    suspend fun getError(code: String): SurfCoreError?

    fun generateCode(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..7)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
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
            !element.className.startsWith("jdk.")
        } ?: stackTrace.first()
        
        return "${relevantElement.className}.${relevantElement.methodName}:${relevantElement.lineNumber}"
    }
}