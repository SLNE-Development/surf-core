package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfCoreErrorLogsTable : AuditableLongIdTable("surf_player_error_logs") {
    val playerUuid = nativeUuid("player_uuid")
    val errorCode = varchar("error_code", 7)
    val errorMessage = text("error_message")
    val server = varchar("server", 255)
}