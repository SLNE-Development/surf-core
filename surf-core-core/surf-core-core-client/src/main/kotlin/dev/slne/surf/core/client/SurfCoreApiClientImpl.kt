package dev.slne.surf.core.client

import dev.slne.surf.core.core.common.SurfCoreApiImpl
import dev.slne.surf.core.core.common.rabbit.packet.player.load.LoadOfflinePlayerNameEntriesRequestPacket

abstract class SurfCoreApiClientImpl : SurfCoreApiImpl() {
    override suspend fun loadOfflinePlayerNameEntries() =
        ClientCoreInstance.rabbitApi.sendRequest(LoadOfflinePlayerNameEntriesRequestPacket()).entries
}