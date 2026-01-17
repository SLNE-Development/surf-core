package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerNameHistoryTable : AuditableLongIdTable("surf_player_name_history") {
    val playerUuid = nativeUuid("surf_player_uuid").references(SurfPlayerTable.uuid)
    val name = varchar("name", 16)
    val lastSeen = offsetDateTime("last_seen")
}