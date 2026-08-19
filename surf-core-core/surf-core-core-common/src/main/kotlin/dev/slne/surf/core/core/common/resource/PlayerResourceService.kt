package dev.slne.surf.core.core.common.resource

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface PlayerResourceService {
    suspend fun findPlayerResource(playerUuid: SerializableUUID): PlayerResource?
    suspend fun findPlayerResource(username: String): PlayerResource?
    suspend fun batchPlayerResources(list: List<SerializableUUID>): List<PlayerResource>
}