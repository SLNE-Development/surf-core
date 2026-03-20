package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.core.api.common.player.history.name.NameHistoryEntry
import dev.slne.surf.core.microservice.database.tables.SurfPlayerNameHistoriesTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object SurfPlayerNameHistoryRepository {
    suspend fun addNameToHistory(
        uuid: UUID,
        historyEntry: NameHistoryEntry
    ) = suspendTransaction {
        SurfPlayerNameHistoriesTable.insert {
            it[playerUuid] = uuid
            it[name] = historyEntry.name
            it[lastSeen] = historyEntry.lastSeen
        }
    }

    suspend fun getNameHistory(
        uuid: UUID
    ): NameHistory = suspendTransaction {
        val entries = SurfPlayerNameHistoriesTable.selectAll()
            .where(SurfPlayerNameHistoriesTable.playerUuid eq uuid).map {
                NameHistoryEntry(
                    name = it[SurfPlayerNameHistoriesTable.name],
                    lastSeen = it[SurfPlayerNameHistoriesTable.lastSeen]
                )
            }.toList()

        NameHistory(entries)
    }
}