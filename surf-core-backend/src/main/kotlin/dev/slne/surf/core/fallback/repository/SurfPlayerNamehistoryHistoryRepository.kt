package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.core.api.common.player.history.name.NameHistoryEntry
import dev.slne.surf.core.fallback.table.SurfPlayerNameHistoryTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

val surfPlayerNameHistoryRepository = SurfPlayerNameHistoryRepository()

class SurfPlayerNameHistoryRepository {
    suspend fun addNameToHistory(uuid: UUID, historyEntry: NameHistoryEntry) =
        suspendTransaction {
            SurfPlayerNameHistoryTable.insert {
                it[playerUuid] = uuid
                it[name] = historyEntry.name
                it[lastSeen] = historyEntry.lastSeen
            }
        }

    suspend fun getNameHistory(uuid: UUID): NameHistory = suspendTransaction {
        val entries = SurfPlayerNameHistoryTable.selectAll()
            .where(SurfPlayerNameHistoryTable.playerUuid eq uuid).map {
                NameHistoryEntry(
                    name = it[SurfPlayerNameHistoryTable.name],
                    lastSeen = it[SurfPlayerNameHistoryTable.lastSeen]
                )
            }.toList()
        NameHistory(entries)
    }
}