package dev.slne.surf.core.fallback.table

import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * Table for storing system-wide errors (not player-specific).
 * Tracks errors from threads and coroutines with deduplication support.
 */
object SystemErrorTable : LongIdTable("system_errors") {
    val errorMessage = text("error_message")
    val stacktrace = text("stacktrace")
    val location = varchar("location", 500)
    val server = varchar("server", 255)
    val firstOccurred = offsetDateTime("first_occurred")
    val lastOccurred = offsetDateTime("last_occurred")
    val occurrenceCount = integer("occurrence_count").default(1)
    
    init {
        // Create a unique index on the combination of message, location, and server
        // This helps identify and deduplicate similar errors
        index(isUnique = true, errorMessage, location, server)
    }
}
