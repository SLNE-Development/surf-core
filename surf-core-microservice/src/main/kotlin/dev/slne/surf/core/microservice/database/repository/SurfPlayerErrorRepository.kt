package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.player.error.SurfPlayerError
import dev.slne.surf.core.microservice.database.tables.SurfPlayerErrorsTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.time.OffsetDateTime
import java.util.*

object SurfPlayerErrorRepository {
    suspend fun saveError(
        playerUuid: UUID,
        occurredOn: String,
        occurredAt: OffsetDateTime,
        staffMessage: String,
        errorCode: String
    ) = suspendTransaction {
        SurfPlayerErrorsTable.insert {
            it[this.playerUuid] = playerUuid
            it[this.occurredOn] = occurredOn
            it[this.createdAt] = occurredAt
            it[this.staffMessage] = staffMessage
            it[this.errorCode] = errorCode
        }.let {
            SurfPlayerError(
                playerUuid,
                occurredOn,
                occurredAt,
                staffMessage,
                errorCode
            )
        }
    }
}