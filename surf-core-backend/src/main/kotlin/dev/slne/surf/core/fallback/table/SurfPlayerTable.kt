package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerTable : AuditableLongIdTable("surf_players") {
    val uuid = nativeUuid("uuid").uniqueIndex()
    val name = varchar("name", 16).nullable()
    val firstSeen = offsetDateTime("first_seen").nullable()
    val lastSeen = offsetDateTime("last_seen").nullable()
    val latestServer = varchar("latest_server", 50).nullable()
    val latestProxy = varchar("latest_proxy", 50).nullable()
}