package dev.slne.surf.core.api.common.server.connection

import dev.slne.surf.api.core.serializer.adventure.component.SerializableComponent
import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import kotlinx.serialization.Serializable

@Serializable
data class SurfServerConnectResult(
    val status: Status,
    val velocityMessage: SerializableComponent?
) {
    var joinCheck: SurfJoinCheckResult? = null
        private set

    constructor(
        status: Status,
        velocityMessage: SerializableComponent?,
        joinCheck: SurfJoinCheckResult?
    ) : this(status, velocityMessage) {
        this.joinCheck = joinCheck
    }

    enum class Status {
        SERVER_NOT_FOUND,
        ALREADY_CONNECTED,
        CONNECTION_CANCELLED,
        CONNECTION_IN_PROGRESS,
        SERVER_DISCONNECTED,
        UNKNOWN_ERROR,
        SUCCESS,
        JOIN_DENIED;
    }

    fun isSuccessful(): Boolean = this.status == Status.SUCCESS
}
