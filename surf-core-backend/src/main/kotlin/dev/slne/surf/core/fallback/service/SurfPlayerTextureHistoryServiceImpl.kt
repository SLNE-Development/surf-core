package dev.slne.surf.core.fallback.service

import com.google.auto.service.AutoService
import com.google.gson.JsonParser
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.core.api.common.player.history.texture.TextureHistoryEntry
import dev.slne.surf.core.core.common.player.history.SurfPlayerTextureHistoryService
import dev.slne.surf.core.fallback.repository.surfPlayerTextureHistoryRepository
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*

@AutoService(SurfPlayerTextureHistoryService::class)
class SurfPlayerTextureHistoryServiceImpl : SurfPlayerTextureHistoryService, Services.Fallback {
    override suspend fun handleNewTexture(
        surfPlayer: SurfPlayer,
        texture: String,
        signature: String
    ) {
        val skinHash = extractSkinHash(texture)
        val latestLogged = getTextureHistory(surfPlayer.uuid).getCurrentTexture()

        if (latestLogged != null && latestLogged.hash == skinHash) {
            return
        }

        surfPlayerTextureHistoryRepository.addTextureToHistory(
            surfPlayer.uuid, TextureHistoryEntry(
                texture = texture,
                signature = signature,
                lastSeen = OffsetDateTime.now(),
                hash = skinHash
            )
        )
    }

    fun extractSkinHash(base64: String): String {
        val json = String(Base64.getDecoder().decode(base64))
        val obj = JsonParser.parseString(json).asJsonObject
        val url = obj["textures"]
            .asJsonObject["SKIN"]
            .asJsonObject["url"]
            .asString
        return url.substringAfterLast("/")
    }


    override suspend fun getTextureHistory(uuid: UUID): TextureHistory =
        surfPlayerTextureHistoryRepository.getTextureHistory(uuid)
}