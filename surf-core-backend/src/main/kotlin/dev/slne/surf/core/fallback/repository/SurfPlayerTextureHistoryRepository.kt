package dev.slne.surf.core.fallback.repository

import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.core.api.common.player.history.texture.TextureHistoryEntry
import dev.slne.surf.core.fallback.table.SurfPlayerTexturesHistoryTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

val surfPlayerTextureHistoryRepository = SurfPlayerTextureHistoryRepository()

class SurfPlayerTextureHistoryRepository {
    suspend fun addTextureToHistory(uuid: UUID, historyEntry: TextureHistoryEntry) =
        suspendTransaction {
            SurfPlayerTexturesHistoryTable.insert {
                it[playerUuid] = uuid
                it[textureValue] = historyEntry.texture
                it[textureSignature] = historyEntry.signature
                it[lastSeen] = historyEntry.lastSeen
            }
        }

    suspend fun getTextureHistory(uuid: UUID): TextureHistory = suspendTransaction {
        val entries = SurfPlayerTexturesHistoryTable.selectAll()
            .where(SurfPlayerTexturesHistoryTable.playerUuid eq uuid).map {
                TextureHistoryEntry(
                    texture = it[SurfPlayerTexturesHistoryTable.textureValue],
                    signature = it[SurfPlayerTexturesHistoryTable.textureSignature],
                    lastSeen = it[SurfPlayerTexturesHistoryTable.lastSeen]
                )
            }.toList()
        TextureHistory(entries)
    }
}