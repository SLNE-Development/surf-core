package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.error.SurfCoreError
import dev.slne.surf.core.fallback.table.SurfCoreErrorLogsTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
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
    ): SurfCoreError = suspendTransaction {
        val timestamp = OffsetDateTime.now()
        SurfCoreErrorLogsTable.insert {
            it[SurfCoreErrorLogsTable.playerUuid] = playerUuid
            it[SurfCoreErrorLogsTable.errorCode] = code
            it[SurfCoreErrorLogsTable.errorMessage] = message
            it[SurfCoreErrorLogsTable.server] = server
            it[SurfCoreErrorLogsTable.timestamp] = timestamp
        }

        SurfCoreError(code, message, server, timestamp)
    }

    suspend fun getErrors(playerUuid: UUID): ObjectList<SurfCoreError> = suspendTransaction {
        SurfCoreErrorLogsTable.selectAll().where(SurfCoreErrorLogsTable.playerUuid eq playerUuid)
            .map {
                SurfCoreError(
                    code = it[SurfCoreErrorLogsTable.errorCode],
                    message = it[SurfCoreErrorLogsTable.errorMessage],
                    server = it[SurfCoreErrorLogsTable.server],
                    timestamp = it[SurfCoreErrorLogsTable.timestamp]
                )
            }.toList().toObjectList()
    }

    suspend fun getError(code: String): SurfCoreError? = suspendTransaction {
        SurfCoreErrorLogsTable.selectAll().where(SurfCoreErrorLogsTable.errorCode eq code).map {
            SurfCoreError(
                code = it[SurfCoreErrorLogsTable.errorCode],
                message = it[SurfCoreErrorLogsTable.errorMessage],
                server = it[SurfCoreErrorLogsTable.server],
                timestamp = it[SurfCoreErrorLogsTable.timestamp]
            )
        }.firstOrNull()
    }
}