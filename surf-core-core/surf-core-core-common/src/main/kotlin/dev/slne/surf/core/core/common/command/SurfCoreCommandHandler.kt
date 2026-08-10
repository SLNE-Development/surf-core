package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.player.SurfPlayerService
import dev.slne.surf.core.core.common.redis.event.SurfPlayerResyncRedisEvent
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience

object SurfCoreCommandHandler {
    fun clearInternalPlayerCache(audience: Audience) {
        SurfPlayerService.clearPlayers()
        audience.sendText {
            appendCorePrefix()
            success("Die Spieler-Caches wurden geleert.")
        }
        CoreInstance.redisApi.publishEvent(SurfPlayerResyncRedisEvent)
        audience.sendText {
            appendCorePrefix()
            info("Die Spieler werden nun auf allen Servern neu synchronisiert.")
        }
    }

    fun configReloaded(audience: Audience) = audience.sendText {
        appendCorePrefix()
        success("Die Konfiguration wurde neu geladen.")
    }
}
