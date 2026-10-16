package dev.slne.surf.core.api.common.join

import java.util.*

/**
 * Decides whether a player could theoretically join the server this listener is registered on.
 *
 * Before a player is transferred to another server or proxy, the requester asks the target
 * for a join check. The target runs **all** registered listeners and sends the combined
 * [SurfJoinCheckResult] back. The transfer is only attempted if no listener denied the join,
 * unless the requester forces it.
 *
 * Register listeners via [dev.slne.surf.core.api.common.SurfCoreApi.registerJoinListener].
 */
fun interface CoreJoinListener {

    /**
     * Name used to identify this listener in denials. Defaults to the class name.
     */
    val name: String get() = javaClass.name

    /**
     * Called on the target server when the player with the given [playerUuid] is about to be
     * transferred to it. The player is usually not connected to this server yet.
     */
    suspend fun checkJoin(playerUuid: UUID): CoreJoinResult
}
