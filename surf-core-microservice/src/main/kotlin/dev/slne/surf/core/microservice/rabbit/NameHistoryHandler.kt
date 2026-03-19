package dev.slne.surf.core.microservice.rabbit

import dev.slne.surf.core.api.common.player.history.name.NameHistoryEntry
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.NameHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.NameHistoryResponsePacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.SaveNameHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.SaveNameHistoryResponsePacket
import dev.slne.surf.core.microservice.database.repository.SurfPlayerNameHistoryRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

object NameHistoryHandler {
    @RabbitHandler
    fun handleNameHistoryRequest(request: NameHistoryRequestPacket) {
        request.launch {
            request.respond(
                NameHistoryResponsePacket(
                    SurfPlayerNameHistoryRepository.getNameHistory(
                        request.uuid
                    )
                )
            )
        }
    }

    @RabbitHandler
    fun handleSaveNameHistoryRequest(request: SaveNameHistoryRequestPacket) {
        request.launch {
            SurfPlayerNameHistoryRepository.addNameToHistory(
                request.uuid, NameHistoryEntry(
                    request.name,
                    OffsetDateTime.now()
                )
            )

            request.respond(SaveNameHistoryResponsePacket())
        }
    }
}