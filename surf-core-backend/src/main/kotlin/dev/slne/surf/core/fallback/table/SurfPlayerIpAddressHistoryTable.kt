package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.inet
import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerIpAddressHistoryTable : AuditableLongIdTable("surf_player_ip_address_history") {
    val playerUuid = nativeUuid("surf_player_uuid").references(SurfPlayerTable.uuid)
    val ipAddress = inet("ip_address")
    val lastSeen = offsetDateTime("last_seen")
}