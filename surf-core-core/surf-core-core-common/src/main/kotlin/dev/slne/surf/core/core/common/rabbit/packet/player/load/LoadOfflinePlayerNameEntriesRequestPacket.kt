package dev.slne.surf.core.core.common.rabbit.packet.player.load

import dev.slne.surf.core.core.common.rabbit.packet.player.ManyOfflinePlayerNamesResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class LoadOfflinePlayerNameEntriesRequestPacket :
    RabbitRequestPacket<ManyOfflinePlayerNamesResponsePacket>()
