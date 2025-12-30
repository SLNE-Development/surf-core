package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerTable : AuditableLongIdTable("surf_players") {
    val uuid = uuid("uuid").uniqueIndex()
    val name = varchar("name", 16).nullable()
    val firstSeen = long("first_seen").nullable()
    val lastSeen = long("last_seen").nullable()
}