package dev.slne.surf.core.microservice.resource.database

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerResourcesTable : AuditableLongIdTable("surf_player_resources") {
    val playerUuid = nativeUuid("player_uuid").uniqueIndex()
    val username = varchar("username", 16).index()
    val skin = largeText("skin")
}