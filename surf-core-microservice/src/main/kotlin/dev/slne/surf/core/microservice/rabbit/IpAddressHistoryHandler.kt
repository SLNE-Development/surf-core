package dev.slne.surf.core.microservice.rabbit

import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistoryEntry
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.IpAddressHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.IpAddressHistoryResponsePacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.SaveIpAddressHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.SaveIpAddressHistoryResponsePacket
import dev.slne.surf.core.microservice.database.repository.SurfPlayerIpAddressHistoryRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

object IpAddressHistoryHandler {
    @RabbitHandler
    fun handleIpAddressHistoryRequest(request: IpAddressHistoryRequestPacket) {
        request.launch {
            request.respond(
                IpAddressHistoryResponsePacket(
                    SurfPlayerIpAddressHistoryRepository.getIpAddressHistory(
                        request.uuid
                    )
                )
            )
        }
    }

    @RabbitHandler
    fun handleSaveIpAddressHistoryRequest(request: SaveIpAddressHistoryRequestPacket) {
        request.launch {
            SurfPlayerIpAddressHistoryRepository.addIpAddressToHistory(
                request.uuid,
                IpAddressHistoryEntry(
                    address = request.ipAddress,
                    lastSeen = OffsetDateTime.now()
                )
            )

            request.respond(SaveIpAddressHistoryResponsePacket())
        }
    }
}