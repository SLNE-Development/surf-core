package dev.slne.surf.core.client.player.history

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.name.NameHistory
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.player.history.SurfPlayerNameHistoryService
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.NameHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.name.SaveNameHistoryRequestPacket
import java.util.*

@AutoService(SurfPlayerNameHistoryService::class)
class SurfPlayerNameHistoryServiceImpl : SurfPlayerNameHistoryService {
    override suspend fun handleNewName(surfPlayer: SurfPlayer) {
        val currentName = surfPlayer.lastKnownName ?: return
        val latestLogged = getNameHistory(surfPlayer.uuid).getCurrentName()

        if (latestLogged != null && latestLogged == currentName) {
            return
        }

        ClientCoreInstance.rabbitApi.sendRequest(
            SaveNameHistoryRequestPacket(
                surfPlayer.uuid,
                currentName
            )
        )
    }

    override suspend fun getNameHistory(uuid: UUID): NameHistory {
        return ClientCoreInstance.rabbitApi.sendRequest(NameHistoryRequestPacket(uuid)).history
    }
}