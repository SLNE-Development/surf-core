package dev.slne.surf.core.client.player.resource

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.resource.PlayerResourceBridge
import dev.slne.surf.core.api.common.util.InternalCoreApi
import net.kyori.adventure.util.Services

@OptIn(InternalCoreApi::class)
@AutoService(PlayerResourceBridge::class)
class PlayerResourceBridgeImpl : PlayerResourceBridge, Services.Fallback {
    override suspend fun resourceFromUuid(playerUuid: SerializableUUID) =
        PlayerResourceCache.resourceFromUuid(playerUuid)

    override suspend fun resourceFromUsername(username: String) =
        PlayerResourceCache.resourceFromUsername(username)

    override suspend fun batch(list: List<SerializableUUID>) = PlayerResourceCache.batch(list)
}