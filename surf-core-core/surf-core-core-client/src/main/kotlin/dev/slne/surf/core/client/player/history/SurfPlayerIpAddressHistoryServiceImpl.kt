package dev.slne.surf.core.client.player.history

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.ip.IpAddressHistory
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.common.player.history.SurfPlayerIpAddressHistoryService
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.IpAddressHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.ip.SaveIpAddressHistoryRequestPacket
import java.util.*

@AutoService(SurfPlayerIpAddressHistoryService::class)
class SurfPlayerIpAddressHistoryServiceImpl : SurfPlayerIpAddressHistoryService {
    override suspend fun handleNewIpAddress(surfPlayer: SurfPlayer) {
        val currentIpAddress = surfPlayer.lastKnownIpAddress ?: return
        val latestLogged = getIpAddressHistory(surfPlayer.uuid).getLatestIpAddress()

        if (latestLogged != null && currentIpAddress == latestLogged) {
            return
        }

        ClientLoader.rabbitApi.sendRequest(
            SaveIpAddressHistoryRequestPacket(
                surfPlayer.uuid,
                currentIpAddress
            )
        )
    }

    override suspend fun getIpAddressHistory(uuid: UUID): IpAddressHistory {
        return ClientLoader.rabbitApi.sendRequest(IpAddressHistoryRequestPacket(uuid)).history
    }
}