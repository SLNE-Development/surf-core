package dev.slne.surf.core.core.common.player.history

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.surfapi.core.api.util.requiredService
import java.util.*

val surfPlayerTextureHistoryService = requiredService<SurfPlayerTextureHistoryService>()

interface SurfPlayerTextureHistoryService {
    suspend fun handleNewTexture(surfPlayer: SurfPlayer, texture: String, signature: String)
    suspend fun getTextureHistory(uuid: UUID): TextureHistory
}