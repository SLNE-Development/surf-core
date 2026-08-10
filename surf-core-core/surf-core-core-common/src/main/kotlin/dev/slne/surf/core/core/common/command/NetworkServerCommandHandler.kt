package dev.slne.surf.core.core.common.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.core.api.common.server.CommonSurfServer
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.core.core.common.redis.event.SurfServerChangeMaxPlayersRedisEvent
import dev.slne.surf.core.core.common.util.appendCorePrefix
import net.kyori.adventure.audience.Audience

object NetworkServerCommandHandler {
    fun changeMaxPlayers(audience: Audience, backend: CommonSurfServer, maxPlayers: Int) {
        CoreInstance.redisApi.publishEvent(
            SurfServerChangeMaxPlayersRedisEvent(backend, maxPlayers)
        )
        audience.sendText {
            appendCorePrefix()
            success("Die maximale Spieleranzahl des Servers ")
            variableValue("'${backend.name}'")
            success(" wurde auf ")
            variableValue(maxPlayers)
            success(" gesetzt.")
        }
    }
}
