package dev.slne.surf.core.api.common.resource

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.core.api.common.util.InternalCoreApi

@OptIn(InternalCoreApi::class)
private val impl = requiredService<PlayerResourceBridge>()

@InternalCoreApi
interface PlayerResourceBridge {
    suspend fun resourceFromUuid(playerUuid: SerializableUUID): PlayerResource?
    suspend fun resourceFromUsername(username: String): PlayerResource?
    suspend fun batch(list: List<SerializableUUID>): List<PlayerResource>

    companion object : PlayerResourceBridge by impl
}