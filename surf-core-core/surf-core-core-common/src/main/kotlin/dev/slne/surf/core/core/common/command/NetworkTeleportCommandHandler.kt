package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.redis.event.SurfPlayerTeleportRequestRedisEvent
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience

object NetworkTeleportCommandHandler {
    suspend fun teleport(
        audience: Audience,
        player: SurfPlayer,
        target: SurfPlayer,
        teleportLocally: suspend () -> Boolean,
    ) {
        audience.sendText {
            appendCorePrefix()
            info("Du wirst zu ")
            variableValue(target.username)
            info(" teleportiert...")
        }
        val targetServer = target.currentServer
        if (targetServer == null) {
            audience.sendText {
                appendCorePrefix()
                error("Der Zielspieler befindet sich auf keinem Server.")
            }
            return
        }

        if (player.currentServer != targetServer) {
            if (player.sendAwaiting(targetServer).isSuccessful()) {
                CoreInstance.redisApi.publishEvent(
                    SurfPlayerTeleportRequestRedisEvent(player, target)
                )
            } else {
                audience.sendText {
                    appendCorePrefix()
                    error("Du konntest nicht zum Zielserver teleportiert werden.")
                }
            }
            return
        }

        if (!teleportLocally()) {
            audience.sendText {
                appendCorePrefix()
                error("Der Zielspieler konnte auf diesem Server nicht gefunden werden.")
            }
        }
    }

    fun sendSuccess(audience: Audience, targetName: String) = audience.sendText {
        appendCorePrefix()
        success("Du wurdest zu ")
        variableValue(targetName)
        success(" teleportiert.")
    }
}
