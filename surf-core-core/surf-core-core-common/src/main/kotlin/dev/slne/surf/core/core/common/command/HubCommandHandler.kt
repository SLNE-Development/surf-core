package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience

object HubCommandHandler {
    suspend fun sendToHub(audience: Audience, player: SurfPlayer) {
        audience.sendText {
            appendCorePrefix()
            info("Du wirst zum Hub gesendet...")
        }
        val playerCounts = SurfPlayerService.playerCountsByServer()
        val servers = SurfCoreApi.getServerByCategory("lobby")
            .sortedBy { playerCounts.getInt(it.name) }
        var success = false
        for (server in servers) {
            if (SurfCoreApi.sendPlayerAwaiting(player, server).isSuccessful()) {
                success = true
                break
            }
        }
        audience.sendText {
            appendCorePrefix()
            if (success) success("Du wurdest erfolgreich zum Hub gesendet.")
            else error("Es konnte kein passender Hub-Server gefunden werden.")
        }
    }
}
