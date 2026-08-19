package dev.slne.surf.core.api.common.resource

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.util.InternalCoreApi
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlayerResource(
    val playerUuid: SerializableUUID,
    val username: String,
    val skin: String
) {
    @OptIn(InternalCoreApi::class)
    companion object {
        suspend fun fromPlayer(player: SurfPlayer) =
            PlayerResourceBridge.resourceFromUuid(player.uuid)

        suspend fun fromPlayer(playerUuid: SerializableUUID) =
            PlayerResourceBridge.resourceFromUuid(playerUuid)

        suspend fun fromPlayer(username: String) =
            PlayerResourceBridge.resourceFromUsername(username)

        suspend fun batch(list: List<UUID>) = PlayerResourceBridge.batch(list)
    }
}
