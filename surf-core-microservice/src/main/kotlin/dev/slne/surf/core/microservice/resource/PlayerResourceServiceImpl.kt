package dev.slne.surf.core.microservice.resource

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.core.common.resource.PlayerResourceService

object PlayerResourceServiceImpl : PlayerResourceService {
    override suspend fun findPlayerResourceByUuid(playerUuid: SerializableUUID) =
        MicroservicePlayerResourceCache.findPlayerResource(playerUuid)

    override suspend fun findPlayerResourceByName(username: String) =
        MicroservicePlayerResourceCache.findPlayerResource(username)

    override suspend fun batchPlayerResources(list: List<SerializableUUID>) =
        MicroservicePlayerResourceCache.batchPlayerResources(list)
}