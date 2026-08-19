package dev.slne.surf.core.client.player.history

import com.google.auto.service.AutoService
import com.google.gson.JsonParser
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.player.history.texture.TextureHistory
import dev.slne.surf.core.api.common.resource.PlayerResource
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.core.common.player.history.SurfPlayerTextureHistoryService
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.SaveTextureHistoryRequestPacket
import dev.slne.surf.core.core.common.rabbit.packet.player.history.texture.TextureHistoryRequestPacket
import dev.slne.surf.core.core.common.resource.PlayerResourceService
import java.util.*

@AutoService(SurfPlayerTextureHistoryService::class)
class SurfPlayerTextureHistoryServiceImpl : SurfPlayerTextureHistoryService {
    private val playerResourceService by lazy {
        ClientCoreInstance.rabbitApi.createRpcService<PlayerResourceService>()
    }

    override suspend fun handleNewTexture(
        surfPlayer: SurfPlayer,
        texture: String,
        signature: String
    ) {
        val skinHash = extractSkinHash(texture)
        val latestLogged = getTextureHistory(surfPlayer.uuid).getCurrentTexture()

        playerResourceService.updatePlayerResource(
            PlayerResource(
                surfPlayer.uuid,
                surfPlayer.username,
                texture
            )
        )


        if (latestLogged == null || latestLogged.hash != skinHash) {
            ClientCoreInstance.rabbitApi.sendRequest(
                SaveTextureHistoryRequestPacket(
                    uuid = surfPlayer.uuid,
                    texture = texture,
                    signature = signature,
                    skinHash = skinHash
                )
            )
        }
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

    override suspend fun getTextureHistory(uuid: UUID): TextureHistory {
        return ClientCoreInstance.rabbitApi.sendRequest(TextureHistoryRequestPacket(uuid)).history
    }
}