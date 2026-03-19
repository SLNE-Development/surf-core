package dev.slne.surf.core.microservice.database.repository

import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.core.api.common.player.history.texture.TextureHistoryEntry
import dev.slne.surf.core.microservice.database.tables.SurfPlayerTexturesHistoriesTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object SurfPlayerTextureHistoryRepository {
    suspend fun addTextureToHistory(
        uuid: UUID,
        historyEntry: TextureHistoryEntry
    ) = suspendTransaction {
        SurfPlayerTexturesHistoriesTable.insert {
            it[playerUuid] = uuid
            it[textureHash] = historyEntry.hash
            it[textureValue] = historyEntry.texture
            it[textureSignature] = historyEntry.signature
            it[lastSeen] = historyEntry.lastSeen
        }
    }

    suspend fun getTextureHistory(
        uuid: UUID
    ): TextureHistory = suspendTransaction {
        val entries = SurfPlayerTexturesHistoriesTable.selectAll()
            .where(SurfPlayerTexturesHistoriesTable.playerUuid eq uuid).map {
                TextureHistoryEntry(
                    hash = it[SurfPlayerTexturesHistoriesTable.textureHash],
                    texture = it[SurfPlayerTexturesHistoriesTable.textureValue],
                    signature = it[SurfPlayerTexturesHistoriesTable.textureSignature],
                    lastSeen = it[SurfPlayerTexturesHistoriesTable.lastSeen]
                )
            }.toList()

        TextureHistory(entries)
    }
}