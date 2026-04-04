package dev.slne.surf.core.microservice.database.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerErrorsTable : AuditableLongIdTable("surf_player_errros") {
    val playerUuid = nativeUuid("player_uuid").references(SurfPlayersTable.uuid)
    val occurredOn = varchar("occurred_on", 64).nullable()
    val staffMessage = largeText("staff_message").nullable()
    val errorCode = varchar("error_code", 64)
}