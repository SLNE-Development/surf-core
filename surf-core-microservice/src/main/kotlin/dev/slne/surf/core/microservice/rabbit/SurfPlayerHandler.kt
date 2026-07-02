package dev.slne.surf.core.microservice.rabbit

import dev.slne.surf.core.core.common.rabbit.packet.player.ManyOfflinePlayerNamesResponsePacket
import dev.slne.surf.core.core.common.rabbit.packet.player.OptionalSurfPlayerResponsePacket
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadOfflinePlayerNameEntriesRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByNameRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadPlayerByUuidRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.save.SaveSurfPlayerRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.save.SaveSurfPlayerResponsePacket
import dev.slne.surf.core.microservice.database.repository.SurfPlayerRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object SurfPlayerHandler {
    @RabbitHandler
    fun handleSavePlayerRequest(request: SaveSurfPlayerRequestPacket) {
        request.launch {
            SurfPlayerRepository.savePlayer(
                uuid = request.uuid,
                name = request.name,
                firstSeen = request.firstSeen,
                lastSeen = request.lastSeen,
                latestServer = request.latestServer,
                latestProxy = request.latestProxy
            )

            request.respond(SaveSurfPlayerResponsePacket())
        }
    }

    @RabbitHandler
    fun handleLoadByUuidRequest(request: LoadPlayerByUuidRequestPacket) {
        request.launch {
            request.respond(
                OptionalSurfPlayerResponsePacket(
                    SurfPlayerRepository.loadPlayerByUuid(
                        request.uuid
                    )
                )
            )
        }
    }

    @RabbitHandler
    fun handleLoadByNameRequest(request: LoadPlayerByNameRequestPacket) {
        request.launch {
            request.respond(
                OptionalSurfPlayerResponsePacket(
                    SurfPlayerRepository.loadPlayerByName(
                        request.name
                    )
                )
            )
        }
    }

    @RabbitHandler
    fun handleLoadOfflinePlayerNameEntriesRequest(request: LoadOfflinePlayerNameEntriesRequestPacket) =
        request.launch {
            request.respond(
                ManyOfflinePlayerNamesResponsePacket(
                    SurfPlayerRepository.loadOfflinePlayerNameEntries()
                )
            )
        }
}