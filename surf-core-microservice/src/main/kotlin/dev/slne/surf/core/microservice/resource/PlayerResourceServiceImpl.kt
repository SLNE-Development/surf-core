package dev.slne.surf.core.microservice.resource

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.core.common.resource.PlayerResourceService
import dev.slne.surf.core.microservice.resource.database.PlayerResourceRepository

object PlayerResourceServiceImpl : PlayerResourceService {
    override suspend fun findPlayerResourceByUuid(playerUuid: SerializableUUID) =
        MicroservicePlayerResourceCache.findPlayerResource(playerUuid)

    override suspend fun findPlayerResourceByName(username: String) =
        MicroservicePlayerResourceCache.findPlayerResource(username)

    override suspend fun batchPlayerResources(list: List<SerializableUUID>) =
        MicroservicePlayerResourceCache.batchPlayerResources(list)

    override suspend fun updatePlayerResource(playerResource: PlayerResource) =
        PlayerResourceRepository.saveResource(playerResource)
}