package dev.slne.surf.core.microservice.database.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.table.AuditableLongIdTable

object SurfPlayerTexturesHistoriesTable : AuditableLongIdTable("surf_player_textures_history") {
    val playerUuid = nativeUuid("surf_player_uuid").references(SurfPlayersTable.uuid)
    val textureHash = text("texture_hash")
    val textureValue = text("texture_value")
    val textureSignature = text("texture_signature")
    val lastSeen = offsetDateTime("last_seen")
}