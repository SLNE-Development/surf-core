package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.fallback.table.SurfPlayerTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

val surfPlayerRepository = SurfPlayerRepository()

class SurfPlayerRepository {
    suspend fun loadPlayerByName(name: String): SurfPlayer? = suspendTransaction {
        SurfPlayerTable.selectAll().where(SurfPlayerTable.name eq name).firstOrNull()?.let {
            createPlayerByRow(it)
        }
    }

    suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer? = suspendTransaction {
        SurfPlayerTable.selectAll().where(SurfPlayerTable.uuid eq uuid).firstOrNull()?.let {
            createPlayerByRow(it)
        }
    }

    suspend fun savePlayer(player: SurfPlayer) = suspendTransaction {
        SurfPlayerTable.upsert {
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
        uuid = row[SurfPlayerTable.uuid],
        lastKnownName = row[SurfPlayerTable.name],
        firstSeen = row[SurfPlayerTable.firstSeen],
        lastSeen = row[SurfPlayerTable.lastSeen],
        currentServer = null,
        currentProxy = null
    )
}