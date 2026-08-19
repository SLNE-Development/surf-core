package dev.slne.surf.core.microservice.resource.source

import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.microservice.database.repository.SurfPlayerRepository
import dev.slne.surf.core.microservice.resource.database.PlayerResourceRepository
import java.util.*

interface RemotePlayerResourceSource {
    suspend fun findPlayerSkin(playerUuid: UUID): String?

    suspend fun findPlayerResource(playerUuid: UUID): PlayerResource? {
        val surfPlayer = SurfPlayerRepository.loadPlayerByUuid(playerUuid) ?: return null
        val skin = findPlayerSkin(playerUuid) ?: return null

        val resource = PlayerResource(
            playerUuid = surfPlayer.uuid,
            username = surfPlayer.username,
            skin = skin
        )

        PlayerResourceRepository.saveResource(resource)
        return resource
    }

    suspend fun findPlayerResource(username: String): PlayerResource? {
        val surfPlayer = SurfPlayerRepository.loadPlayerByName(username) ?: return null
        val skin = findPlayerSkin(surfPlayer.uuid) ?: return null

        val resource = PlayerResource(
            playerUuid = surfPlayer.uuid,
            username = surfPlayer.username,
            skin = skin
        )

        PlayerResourceRepository.saveResource(resource)
        return resource
    }
}