package dev.slne.surf.core.microservice.rabbit

import dev.slne.surf.core.core.common.rabbit.packet.player.error.SaveSurfPlayerErrorRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.error.SingleSurfPlayerErrorResponsePacket
import dev.slne.surf.core.microservice.database.repository.SurfPlayerErrorRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object SurfPlayerErrorHandler {
    @RabbitHandler
    fun handleSavePlayerError(packet: SaveSurfPlayerErrorRequestPacket) = packet.launch {
        packet.respond(
            SingleSurfPlayerErrorResponsePacket(
                SurfPlayerErrorRepository.saveError(
                    playerUuid = packet.playerUuid,
                    occurredOn = packet.occurredOn,
                    occurredAt = packet.occurredAt,
                    staffMessage = packet.staffMessage,
                    errorCode = packet.errorCode
                )
            )
        )
    }
}