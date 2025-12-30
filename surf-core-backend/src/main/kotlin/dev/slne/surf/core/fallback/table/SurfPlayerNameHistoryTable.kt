package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerNameHistoryTable : AuditableLongIdTable("surf_player_namehistory") {
    val playerUuid = uuid("player_uuid").references(SurfPlayerTable.uuid, ReferenceOption.CASCADE)
    val name = varchar("name", 16)
    val changedAt = long("changed_at").nullable()
}