package dev.slne.surf.core.core.common.join

import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import net.kyori.adventure.text.Component
import java.util.*

abstract class AlreadyConnectedJoinListener : CoreJoinListener {
    override val name = NAME

    protected abstract fun isConnected(playerUuid: UUID): Boolean

    override suspend fun checkJoin(playerUuid: UUID): CoreJoinResult {
        if (!isConnected(playerUuid)) {
            return CoreJoinResult.allowed()
        }

        return CoreJoinResult.denied(Component.text("Du bist bereits verbunden!"))
    }

    companion object {
        const val NAME = "already-connected"
    }
}

fun SurfJoinCheckResult.isOnlyAlreadyConnected() =
    status == SurfJoinCheckResult.Status.DENIED &&
            denials.all { it.listener == AlreadyConnectedJoinListener.NAME }
