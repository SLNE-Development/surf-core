package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerNameHistoryTable : AuditableLongIdTable("surf_player_name_history") {
    val playerUuid = uuid("surf_player_uuid").references(SurfPlayerTable.uuid)
    val name = varchar("name", 16)
}