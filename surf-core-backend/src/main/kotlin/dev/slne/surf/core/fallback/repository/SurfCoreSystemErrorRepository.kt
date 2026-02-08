package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.error.SurfCoreSystemError
import dev.slne.surf.core.fallback.table.SurfCoreSystemErrorTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.lessEq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime
import java.util.*

val surfCoreSystemErrorRepository = SurfCoreSystemErrorRepository()

class SurfCoreSystemErrorRepository {
    suspend fun logError(
        message: String,
        stacktrace: String,
        location: String,
        server: String
    ): SurfCoreSystemError = suspendTransaction {
        val now = OffsetDateTime.now()

        val existingError = SurfCoreSystemErrorTable.selectAll()
            .where(
                (SurfCoreSystemErrorTable.errorMessage eq message) and
                        (SurfCoreSystemErrorTable.location eq location) and
                        (SurfCoreSystemErrorTable.server eq server) and
                        (SurfCoreSystemErrorTable.lastOccurred lessEq now.minusDays(1))
            )
            .map { row ->
                SurfCoreSystemError(
                    uuid = row[SurfCoreSystemErrorTable.uuid],
                    errorMessage = row[SurfCoreSystemErrorTable.errorMessage],
                    stacktrace = row[SurfCoreSystemErrorTable.stacktrace],
                    location = row[SurfCoreSystemErrorTable.location],
                    server = row[SurfCoreSystemErrorTable.server],
                    firstOccurred = row[SurfCoreSystemErrorTable.firstOccurred],
                    lastOccurred = row[SurfCoreSystemErrorTable.lastOccurred],
                    occurrenceCount = row[SurfCoreSystemErrorTable.occurrenceCount]
                )
            }
            .firstOrNull()

        SurfCoreSystemErrorTable.upsert {
            it[SurfCoreSystemErrorTable.uuid] = existingError?.uuid ?: UUID.randomUUID()
            it[SurfCoreSystemErrorTable.errorMessage] = message
            it[SurfCoreSystemErrorTable.stacktrace] = stacktrace
            it[SurfCoreSystemErrorTable.location] = location
            it[SurfCoreSystemErrorTable.server] = server
            it[SurfCoreSystemErrorTable.firstOccurred] = existingError?.firstOccurred ?: now
            it[SurfCoreSystemErrorTable.lastOccurred] = now
            it[SurfCoreSystemErrorTable.occurrenceCount] = (existingError?.occurrenceCount ?: 0) + 1
        }

        val resultError = SurfCoreSystemErrorTable.selectAll()
            .where(
                (SurfCoreSystemErrorTable.errorMessage eq message) and
                        (SurfCoreSystemErrorTable.location eq location) and
                        (SurfCoreSystemErrorTable.server eq server)
            )
            .map { row ->
                SurfCoreSystemError(
                    uuid = row[SurfCoreSystemErrorTable.uuid],
                    errorMessage = row[SurfCoreSystemErrorTable.errorMessage],
                    stacktrace = row[SurfCoreSystemErrorTable.stacktrace],
                    location = row[SurfCoreSystemErrorTable.location],
                    server = row[SurfCoreSystemErrorTable.server],
                    firstOccurred = row[SurfCoreSystemErrorTable.firstOccurred],
                    lastOccurred = row[SurfCoreSystemErrorTable.lastOccurred],
                    occurrenceCount = row[SurfCoreSystemErrorTable.occurrenceCount]
                )
            }
            .firstOrNull()

        return@suspendTransaction resultError
            ?: error("Failed to retrieve error after upsert")
    }

    suspend fun getAllErrors(): ObjectList<SurfCoreSystemError> = suspendTransaction {
        SurfCoreSystemErrorTable.selectAll()
            .map { row ->
                SurfCoreSystemError(
                    uuid = row[SurfCoreSystemErrorTable.uuid],
                    errorMessage = row[SurfCoreSystemErrorTable.errorMessage],
                    stacktrace = row[SurfCoreSystemErrorTable.stacktrace],
                    location = row[SurfCoreSystemErrorTable.location],
                    server = row[SurfCoreSystemErrorTable.server],
                    firstOccurred = row[SurfCoreSystemErrorTable.firstOccurred],
                    lastOccurred = row[SurfCoreSystemErrorTable.lastOccurred],
                    occurrenceCount = row[SurfCoreSystemErrorTable.occurrenceCount]
                )
            }.toList().toObjectList()
    }

    suspend fun getError(id: Long): SurfCoreSystemError? = suspendTransaction {
        SurfCoreSystemErrorTable.selectAll()
            .where(SurfCoreSystemErrorTable.id eq id)
            .map { row ->
                SurfCoreSystemError(
                    uuid = row[SurfCoreSystemErrorTable.uuid],
                    errorMessage = row[SurfCoreSystemErrorTable.errorMessage],
                    stacktrace = row[SurfCoreSystemErrorTable.stacktrace],
                    location = row[SurfCoreSystemErrorTable.location],
                    server = row[SurfCoreSystemErrorTable.server],
                    firstOccurred = row[SurfCoreSystemErrorTable.firstOccurred],
                    lastOccurred = row[SurfCoreSystemErrorTable.lastOccurred],
                    occurrenceCount = row[SurfCoreSystemErrorTable.occurrenceCount]
                )
            }.firstOrNull()
    }
}
