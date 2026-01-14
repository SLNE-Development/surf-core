package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.player.history.IpAddressHistory
import dev.slne.surf.core.api.common.player.history.entry.IpAddressHistoryEntry
import dev.slne.surf.core.fallback.table.SurfPlayerIpAddressHistoryTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

val surfPlayerIpAddressHistoryRepository = SurfPlayerIpAddressHistoryRepository()

class SurfPlayerIpAddressHistoryRepository {
    suspend fun addIpAddressToHistory(uuid: UUID, historyEntry: IpAddressHistoryEntry) =
        suspendTransaction {
            SurfPlayerIpAddressHistoryTable.insert {
                it[playerUuid] = uuid
                it[ipAddress] = historyEntry.address
                it[lastSeen] = historyEntry.lastSeen
            }
        }

    suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory = suspendTransaction {
        val entries = SurfPlayerIpAddressHistoryTable.selectAll()
            .where(SurfPlayerIpAddressHistoryTable.playerUuid eq uuid).map {
            IpAddressHistoryEntry(
                address = it[SurfPlayerIpAddressHistoryTable.ipAddress],
                lastSeen = it[SurfPlayerIpAddressHistoryTable.lastSeen]
            )
        }.toList()
        IpAddressHistory(entries)
    }
}