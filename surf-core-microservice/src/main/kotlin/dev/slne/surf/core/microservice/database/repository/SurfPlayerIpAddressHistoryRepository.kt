package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistoryEntry
import dev.slne.surf.core.microservice.database.tables.SurfPlayerIpAddressHistoriesTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object SurfPlayerIpAddressHistoryRepository {
    suspend fun addIpAddressToHistory(
        uuid: UUID,
        historyEntry: IpAddressHistoryEntry
    ) = suspendTransaction {
        SurfPlayerIpAddressHistoriesTable.insert {
            it[playerUuid] = uuid
            it[ipAddress] = historyEntry.address
            it[lastSeen] = historyEntry.lastSeen
        }
    }

    suspend fun getIpAddressHistory(
        uuid: UUID
    ): IpAddressHistory = suspendTransaction {
        val entries = SurfPlayerIpAddressHistoriesTable.selectAll()
            .where(SurfPlayerIpAddressHistoriesTable.playerUuid eq uuid).map {
                IpAddressHistoryEntry(
                    address = it[SurfPlayerIpAddressHistoriesTable.ipAddress],
                    lastSeen = it[SurfPlayerIpAddressHistoriesTable.lastSeen]
                )
            }.toList()
        
        IpAddressHistory(entries)
    }
}