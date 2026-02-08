package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.core.fallback.table.SystemErrorTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime

val systemErrorRepository = SystemErrorRepository()

/**
 * Repository for managing system-wide errors.
 * Handles error logging with automatic deduplication.
 */
class SystemErrorRepository {
    /**
     * Logs a system error. If a similar error already exists (same message, location, and server),
     * it updates the lastOccurred timestamp and increments the occurrence count.
     */
    suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SystemError = suspendTransaction {
        val now = OffsetDateTime.now()
        
        // Check if a similar error already exists
        val existingError = SystemErrorTable.selectAll()
            .where(
                (SystemErrorTable.errorMessage eq message) and
                (SystemErrorTable.location eq location) and
                (SystemErrorTable.server eq server)
            )
            .map { row ->
                SystemError(
                    id = row[SystemErrorTable.id].value,
                    errorMessage = row[SystemErrorTable.errorMessage],
                    stacktrace = row[SystemErrorTable.stacktrace],
                    location = row[SystemErrorTable.location],
                    server = row[SystemErrorTable.server],
                    firstOccurred = row[SystemErrorTable.firstOccurred],
                    lastOccurred = row[SystemErrorTable.lastOccurred],
                    occurrenceCount = row[SystemErrorTable.occurrenceCount]
                )
            }
            .firstOrNull()
        
        if (existingError != null) {
            // Update the existing error with new lastOccurred and increment count
            SystemErrorTable.update(
                where = { SystemErrorTable.id eq existingError.id }
            ) {
                it[SystemErrorTable.lastOccurred] = now
                it[SystemErrorTable.occurrenceCount] = existingError.occurrenceCount + 1
                it[SystemErrorTable.stacktrace] = stacktrace // Update stacktrace in case it's slightly different
            }
            
            return@suspendTransaction existingError.copy(
                lastOccurred = now,
                occurrenceCount = existingError.occurrenceCount + 1,
                stacktrace = stacktrace
            )
        } else {
            // Insert new error
            val insertResult = SystemErrorTable.insert {
                it[SystemErrorTable.errorMessage] = message
                it[SystemErrorTable.stacktrace] = stacktrace
                it[SystemErrorTable.location] = location
                it[SystemErrorTable.server] = server
                it[SystemErrorTable.firstOccurred] = now
                it[SystemErrorTable.lastOccurred] = now
                it[SystemErrorTable.occurrenceCount] = 1
            }
            
            val id = insertResult[SystemErrorTable.id]

            return@suspendTransaction SystemError(
                id = id.value,
                errorMessage = message,
                stacktrace = stacktrace,
                location = location,
                server = server,
                firstOccurred = now,
                lastOccurred = now,
                occurrenceCount = 1
            )
        }
    }

    /**
     * Gets all system errors.
     */
    suspend fun getAllErrors(): ObjectList<SystemError> = suspendTransaction {
        SystemErrorTable.selectAll()
            .map { row ->
                SystemError(
                    id = row[SystemErrorTable.id].value,
                    errorMessage = row[SystemErrorTable.errorMessage],
                    stacktrace = row[SystemErrorTable.stacktrace],
                    location = row[SystemErrorTable.location],
                    server = row[SystemErrorTable.server],
                    firstOccurred = row[SystemErrorTable.firstOccurred],
                    lastOccurred = row[SystemErrorTable.lastOccurred],
                    occurrenceCount = row[SystemErrorTable.occurrenceCount]
                )
            }.toList().toObjectList()
    }

    /**
     * Gets a specific error by ID.
     */
    suspend fun getError(id: Long): SystemError? = suspendTransaction {
        SystemErrorTable.selectAll()
            .where(SystemErrorTable.id eq id)
            .map { row ->
                SystemError(
                    id = row[SystemErrorTable.id].value,
                    errorMessage = row[SystemErrorTable.errorMessage],
                    stacktrace = row[SystemErrorTable.stacktrace],
                    location = row[SystemErrorTable.location],
                    server = row[SystemErrorTable.server],
                    firstOccurred = row[SystemErrorTable.firstOccurred],
                    lastOccurred = row[SystemErrorTable.lastOccurred],
                    occurrenceCount = row[SystemErrorTable.occurrenceCount]
                )
            }.firstOrNull()
    }
}
