package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.name.NameHistory
import dev.slne.surf.core.api.common.player.name.NameHistoryEntry
import dev.slne.surf.core.fallback.table.SurfPlayerNameHistoryTable
import dev.slne.surf.core.fallback.table.SurfPlayerTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

val surfPlayerRepository = SurfPlayerRepository()

class SurfPlayerRepository {
    suspend fun loadPlayerByName(name: String): SurfPlayer? = suspendTransaction {
        val playerId =
            SurfPlayerNameHistoryTable.selectAll().where(SurfPlayerNameHistoryTable.name eq name)
                .firstOrNull()?.get(
                    SurfPlayerNameHistoryTable.playerUuid
                ) ?: return@suspendTransaction null

        val history = SurfPlayerNameHistoryTable.selectAll()
            .where(SurfPlayerNameHistoryTable.playerUuid eq playerId).map {
                createNameHistoryEntry(it)
            }.toList()

        SurfPlayerTable.selectAll().where(SurfPlayerTable.uuid eq playerId).firstOrNull()?.let {
            createPlayerByRowAndHistory(it, NameHistory(history))
        }
    }

    suspend fun loadPlayerByUuid(uuid: UUID): SurfPlayer? = suspendTransaction {
        val history = SurfPlayerNameHistoryTable.selectAll()
            .where(SurfPlayerNameHistoryTable.playerUuid eq uuid).map {
                createNameHistoryEntry(it)
            }.toList().let {
                NameHistory(it)
            }

        SurfPlayerTable.selectAll().where(SurfPlayerTable.uuid eq uuid).firstOrNull()?.let {
            createPlayerByRowAndHistory(it, history)
        }
    }

    suspend fun savePlayer(player: SurfPlayer) = suspendTransaction {
        SurfPlayerTable.upsert {
            it[uuid] = player.uuid
        }
        Unit
    }

    private fun createPlayerByRowAndHistory(row: ResultRow, history: NameHistory) = row.let {
        SurfPlayer(
            row[SurfPlayerTable.uuid],
            row[SurfPlayerTable.firstSeen],
            row[SurfPlayerTable.lastSeen],
            history
        )
    }

    private fun createNameHistory(rows: List<ResultRow>) = rows.let {
        NameHistory(
            it.map { row ->
                NameHistoryEntry(
                    row[SurfPlayerNameHistoryTable.name],
                    row[SurfPlayerNameHistoryTable.changedAt]
                )
            }
        )
    }

    private fun createNameHistoryEntry(row: ResultRow) = row.let {
        NameHistoryEntry(
            row[SurfPlayerNameHistoryTable.name],
            row[SurfPlayerNameHistoryTable.changedAt]
        )
    }
}