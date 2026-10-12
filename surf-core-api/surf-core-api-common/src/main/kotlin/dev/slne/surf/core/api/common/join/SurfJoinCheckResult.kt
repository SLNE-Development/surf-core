package dev.slne.surf.core.api.common.join

import dev.slne.surf.api.core.serializer.adventure.component.SerializableComponent
import kotlinx.serialization.Serializable

/**
 * Combined result of all [CoreJoinListener]s of a target server.
 */
@Serializable
data class SurfJoinCheckResult(
    val status: Status,
    val denials: List<Denial> = emptyList()
) {
    enum class Status {
        /** All listeners allowed the join. */
        ALLOWED,

        /** At least one listener denied the join, see [denials]. */
        DENIED,

        /** The target server did not acknowledge the check request. */
        TARGET_UNREACHABLE,

        /** The target server acknowledged the check request but never sent a status. */
        TIMEOUT;
    }

    @Serializable
    data class Denial(
        val listener: String,
        val reason: SerializableComponent?
    )

    fun isAllowed(): Boolean = this.status == Status.ALLOWED

    companion object {
        val ALLOWED = SurfJoinCheckResult(Status.ALLOWED)
        val TARGET_UNREACHABLE = SurfJoinCheckResult(Status.TARGET_UNREACHABLE)
        val TIMEOUT = SurfJoinCheckResult(Status.TIMEOUT)
    }
}
