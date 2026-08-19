package dev.slne.surf.core.microservice.resource.database

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.inList
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object PlayerResourceRepository {
    suspend fun findPlayerResource(playerUuid: SerializableUUID) = suspendTransaction {
        SurfPlayerResourcesTable.selectAll()
            .where(SurfPlayerResourcesTable.playerUuid eq playerUuid)
            .firstOrNull()
            ?.let { createPlayerResource(it) }
    }

    suspend fun findPlayerResource(username: String) = suspendTransaction {
        SurfPlayerResourcesTable.selectAll()
            .where(SurfPlayerResourcesTable.username eq username)
            .firstOrNull()
            ?.let { createPlayerResource(it) }
    }

    suspend fun batchPlayerResources(
        list: List<SerializableUUID>
    ): Pair<List<PlayerResource>, List<UUID>> = suspendTransaction {
        val resources = SurfPlayerResourcesTable
            .selectAll()
            .where(SurfPlayerResourcesTable.playerUuid inList list)
            .map(::createPlayerResource)
            .toList()

        val found = resources.map { it.playerUuid }
        val missing = list
            .filterNot(found::contains)

        resources to missing
    }

    suspend fun saveResource(resource: PlayerResource) = suspendTransaction {
        SurfPlayerResourcesTable.upsert(SurfPlayerResourcesTable.playerUuid) {
            it[playerUuid] = resource.playerUuid
            it[username] = resource.username
            it[skin] = resource.skin
        }
        Unit
    }

    private fun createPlayerResource(row: ResultRow) = PlayerResource(
        row[SurfPlayerResourcesTable.playerUuid],
        row[SurfPlayerResourcesTable.username],
        row[SurfPlayerResourcesTable.skin]
    )
}