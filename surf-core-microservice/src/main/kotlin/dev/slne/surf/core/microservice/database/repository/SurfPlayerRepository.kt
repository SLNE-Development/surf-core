package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.microservice.database.tables.SurfPlayersTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime
import java.util.*

object SurfPlayerRepository {
    suspend fun loadPlayerByName(
        name: String
    ): SurfPlayer? = suspendTransaction {
        SurfPlayersTable.selectAll().where(SurfPlayersTable.name eq name).firstOrNull()?.let {
            createPlayerByRow(it)
        }
    }

    suspend fun loadPlayerByUuid(
        uuid: UUID
    ): SurfPlayer? = suspendTransaction {
        SurfPlayersTable.selectAll().where(SurfPlayersTable.uuid eq uuid).firstOrNull()?.let {
            createPlayerByRow(it)
        }
    }

    suspend fun savePlayer(
        uuid: UUID,
        name: String?,
        firstSeen: OffsetDateTime?,
        lastSeen: OffsetDateTime?,
        latestServer: String?,
        latestProxy: String?,
    ) = suspendTransaction {
        SurfPlayersTable.upsert(SurfPlayersTable.uuid) {
            it[this.uuid] = uuid
            it[this.name] = name
            it[this.firstSeen] = firstSeen
            it[this.lastSeen] = lastSeen
            it[this.latestServer] = latestServer
            it[this.latestProxy] = latestProxy
        }
        Unit
    }

    suspend fun loadOfflinePlayerNameEntries() = suspendTransaction {
        SurfPlayersTable.select(SurfPlayersTable.name, SurfPlayersTable.uuid).mapNotNull {
            val name = it[SurfPlayersTable.name]
            val uuid = it[SurfPlayersTable.uuid]

            if (name == null) {
                return@mapNotNull null
            }

            OfflinePlayerNameCache.Entry(
                uuid,
                name
            )
        }.toList()
    }

    private fun createPlayerByRow(row: ResultRow) = SurfPlayer(
        uuid = row[SurfPlayersTable.uuid],
        lastKnownName = row[SurfPlayersTable.name],
        firstSeen = row[SurfPlayersTable.firstSeen],
        lastSeen = row[SurfPlayersTable.lastSeen],
        currentServerName = null,
        currentProxyName = null,
        transferred = false
    )
}