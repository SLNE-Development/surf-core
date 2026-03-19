package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.microservice.database.tables.SurfPlayersTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.firstOrNull
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
        player: SurfPlayer
    ) = suspendTransaction {
        SurfPlayersTable.upsert {
            it[uuid] = player.uuid
            it[name] = player.lastKnownName
            it[firstSeen] = player.firstSeen
            it[lastSeen] = player.lastSeen
            it[latestServer] = player.currentServer?.name
            it[latestProxy] = player.currentProxy?.name
        }
        Unit
    }

    private fun createPlayerByRow(row: ResultRow) = SurfPlayer(
        uuid = row[SurfPlayersTable.uuid],
        lastKnownName = row[SurfPlayersTable.name],
        firstSeen = row[SurfPlayersTable.firstSeen],
        lastSeen = row[SurfPlayersTable.lastSeen],
        currentServer = null,
        currentProxy = null,
        transferred = false
    )
}