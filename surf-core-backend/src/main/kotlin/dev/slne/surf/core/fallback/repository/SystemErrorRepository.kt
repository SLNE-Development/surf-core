package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.error.SystemError
import dev.slne.surf.core.fallback.table.SystemErrorTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime

val systemErrorRepository = SystemErrorRepository()

class SystemErrorRepository {
    suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SystemError = suspendTransaction {
        val now = OffsetDateTime.now()
        
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
        
        SystemErrorTable.upsert {
            it[SystemErrorTable.errorMessage] = message
            it[SystemErrorTable.stacktrace] = stacktrace
            it[SystemErrorTable.location] = location
            it[SystemErrorTable.server] = server
            it[SystemErrorTable.firstOccurred] = existingError?.firstOccurred ?: now
            it[SystemErrorTable.lastOccurred] = now
            it[SystemErrorTable.occurrenceCount] = (existingError?.occurrenceCount ?: 0) + 1
        }
        
        val resultError = SystemErrorTable.selectAll()
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
        
        return@suspendTransaction resultError ?: throw IllegalStateException("Failed to retrieve error after upsert")
    }

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
