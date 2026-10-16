package dev.slne.surf.core.core.common.join

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

object CoreJoinListenerRegistry {
    private val log = logger()
    private val listeners = CopyOnWriteArrayList<CoreJoinListener>()

    fun register(listener: CoreJoinListener) {
        listeners.addIfAbsent(listener)
    }

    fun unregister(listener: CoreJoinListener) {
        listeners.remove(listener)
    }

    suspend fun check(playerUuid: UUID): SurfJoinCheckResult {
        val denials = mutableListOf<SurfJoinCheckResult.Denial>()

        for (listener in listeners) {
            val result = try {
                withTimeoutOrNull(5.seconds) { listener.checkJoin(playerUuid) }
                    ?: CoreJoinResult.denied(Component.text("Zeitüberschreitung bei der Prüfung"))
                        .also {
                            log.atWarning()
                                .log("Join listener %s timed out for %s", listener.name, playerUuid)
                        }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Join listener %s failed for %s", listener.name, playerUuid)
                CoreJoinResult.denied(Component.text("Interner Fehler bei der Prüfung"))
            }

            if (result is CoreJoinResult.Denied) {
                denials += SurfJoinCheckResult.Denial(listener.name, result.reason)
            }
        }

        return if (denials.isEmpty()) {
            SurfJoinCheckResult.ALLOWED
        } else {
            SurfJoinCheckResult(SurfJoinCheckResult.Status.DENIED, denials)
        }
    }
}
