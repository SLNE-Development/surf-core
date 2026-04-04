package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.api.core.api.util.requiredService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import java.util.*

private val service = requiredService<SurfPlayerTextureHistoryService>()

interface SurfPlayerTextureHistoryService {
    suspend fun handleNewTexture(surfPlayer: SurfPlayer, texture: String, signature: String)
    suspend fun getTextureHistory(uuid: UUID): TextureHistory

    companion object : SurfPlayerTextureHistoryService by service {
        val INSTANCE get() = service
    }
}