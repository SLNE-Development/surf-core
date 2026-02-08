package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.fallback.table.SurfCoreErrorLogsTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
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
import java.util.*

val surfCoreErrorLoggingRepository = SurfCoreErrorLoggingRepository()

class SurfCoreErrorLoggingRepository {
    suspend fun logError(
        playerUuid: UUID,
        code: String,
        message: String,
        server: String,
        stacktrace: String,
        location: String,
    ): SurfCoreError = suspendTransaction {
        val timestamp = OffsetDateTime.now()
        
        // Check if a similar error already exists (same message, location, and server)
        val existingError = SurfCoreErrorLogsTable.selectAll()
            .where(
                (SurfCoreErrorLogsTable.errorMessage eq message) and
                (SurfCoreErrorLogsTable.location eq location) and
                (SurfCoreErrorLogsTable.server eq server)
            )
            .map {
                SurfCoreError(
                    playerUuid = it[SurfCoreErrorLogsTable.playerUuid],
                    code = it[SurfCoreErrorLogsTable.errorCode],
                    message = it[SurfCoreErrorLogsTable.errorMessage],
                    server = it[SurfCoreErrorLogsTable.server],
                    timestamp = it[SurfCoreErrorLogsTable.timestamp],
                    stacktrace = it[SurfCoreErrorLogsTable.stacktrace],
                    location = it[SurfCoreErrorLogsTable.location],
                    lastOccurred = it[SurfCoreErrorLogsTable.lastOccurred],
                    occurrenceCount = it[SurfCoreErrorLogsTable.occurrenceCount]
                )
            }
            .firstOrNull()
        
        if (existingError != null) {
            // Update the existing error with new lastOccurred and increment count
            SurfCoreErrorLogsTable.update(
                where = { SurfCoreErrorLogsTable.errorCode eq existingError.code }
            ) {
                it[SurfCoreErrorLogsTable.lastOccurred] = timestamp
                it[SurfCoreErrorLogsTable.occurrenceCount] = existingError.occurrenceCount + 1
            }
            
            return@suspendTransaction existingError.copy(
                lastOccurred = timestamp,
                occurrenceCount = existingError.occurrenceCount + 1
            )
        } else {
            // Insert new error
            SurfCoreErrorLogsTable.insert {
                it[SurfCoreErrorLogsTable.playerUuid] = playerUuid
                it[SurfCoreErrorLogsTable.errorCode] = code
                it[SurfCoreErrorLogsTable.errorMessage] = message
                it[SurfCoreErrorLogsTable.server] = server
                it[SurfCoreErrorLogsTable.timestamp] = timestamp
                it[SurfCoreErrorLogsTable.stacktrace] = stacktrace
                it[SurfCoreErrorLogsTable.location] = location
                it[SurfCoreErrorLogsTable.lastOccurred] = timestamp
                it[SurfCoreErrorLogsTable.occurrenceCount] = 1
            }

            return@suspendTransaction SurfCoreError(
                playerUuid,
                code,
                message,
                server,
                timestamp,
                stacktrace,
                location,
                timestamp,
                1
            )
        }
    }

    suspend fun getErrors(playerUuid: UUID): ObjectList<SurfCoreError> = suspendTransaction {
        SurfCoreErrorLogsTable.selectAll().where(SurfCoreErrorLogsTable.playerUuid eq playerUuid)
            .map {
                SurfCoreError(
                    playerUuid = it[SurfCoreErrorLogsTable.playerUuid],
                    code = it[SurfCoreErrorLogsTable.errorCode],
                    message = it[SurfCoreErrorLogsTable.errorMessage],
                    server = it[SurfCoreErrorLogsTable.server],
                    timestamp = it[SurfCoreErrorLogsTable.timestamp],
                    stacktrace = it[SurfCoreErrorLogsTable.stacktrace],
                    location = it[SurfCoreErrorLogsTable.location],
                    lastOccurred = it[SurfCoreErrorLogsTable.lastOccurred],
                    occurrenceCount = it[SurfCoreErrorLogsTable.occurrenceCount]
                )
            }.toList().toObjectList()
    }

    suspend fun getError(code: String): SurfCoreError? = suspendTransaction {
        SurfCoreErrorLogsTable.selectAll().where(SurfCoreErrorLogsTable.errorCode eq code).map {
            SurfCoreError(
                playerUuid = it[SurfCoreErrorLogsTable.playerUuid],
                code = it[SurfCoreErrorLogsTable.errorCode],
                message = it[SurfCoreErrorLogsTable.errorMessage],
                server = it[SurfCoreErrorLogsTable.server],
                timestamp = it[SurfCoreErrorLogsTable.timestamp],
                stacktrace = it[SurfCoreErrorLogsTable.stacktrace],
                location = it[SurfCoreErrorLogsTable.location],
                lastOccurred = it[SurfCoreErrorLogsTable.lastOccurred],
                occurrenceCount = it[SurfCoreErrorLogsTable.occurrenceCount]
            )
        }.firstOrNull()
    }
}