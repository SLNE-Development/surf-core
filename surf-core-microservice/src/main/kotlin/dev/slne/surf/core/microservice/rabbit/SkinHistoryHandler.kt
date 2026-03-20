package dev.slne.surf.core.microservice.rabbit

import dev.slne.surf.core.api.common.player.history.texture.TextureHistoryEntry
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.SaveTextureHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.SaveTextureHistoryResponsePacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.TextureHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.TextureHistoryResponsePacket
import dev.slne.surf.core.microservice.database.repository.SurfPlayerTextureHistoryRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

object SkinHistoryHandler {
    @RabbitHandler
    fun handleTextureHistoryRequest(request: TextureHistoryRequestPacket) {
        request.launch {
            request.respond(
                TextureHistoryResponsePacket(
                    SurfPlayerTextureHistoryRepository.getTextureHistory(
                        request.uuid
                    )
                )
            )
        }
    }

    @RabbitHandler
    fun handleSaveTextureHistoryRequest(request: SaveTextureHistoryRequestPacket) {
        request.launch {
            SurfPlayerTextureHistoryRepository.addTextureToHistory(
                request.uuid,
                TextureHistoryEntry(
                    texture = request.texture,
                    signature = request.signature,
                    lastSeen = OffsetDateTime.now(),
                    hash = request.skinHash
                )
            )

            request.respond(SaveTextureHistoryResponsePacket())
        }
    }
}