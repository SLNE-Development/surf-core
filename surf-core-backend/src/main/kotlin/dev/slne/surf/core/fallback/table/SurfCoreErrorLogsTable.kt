package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object SurfCoreErrorLogsTable : LongIdTable("surf_player_error_logs") {
    val playerUuid = nativeUuid("player_uuid")
    val errorCode = varchar("error_code", 7)
    val errorMessage = text("error_message")
    val server = varchar("server", 255)
    val timestamp = offsetDateTime("timestamp")
    val stacktrace = text("stacktrace")
    val location = varchar("location", 500)
    val lastOccurred = offsetDateTime("last_occurred")
    val occurrenceCount = integer("occurrence_count").default(1)
    
    init {
        // Create a unique index on the combination of message, location, and server
        // This helps identify duplicate errors
        index(isUnique = false, errorMessage, location, server)
    }
}