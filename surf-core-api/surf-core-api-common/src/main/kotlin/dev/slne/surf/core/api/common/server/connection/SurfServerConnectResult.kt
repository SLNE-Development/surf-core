package dev.slne.surf.core.api.common.server.connection

import dev.slne.surf.api.core.serializer.adventure.component.SerializableComponent
import kotlinx.serialization.Serializable

@Serializable
data class SurfServerConnectResult(
    val status: Status,
    val velocityMessage: SerializableComponent?
) {
    enum class Status {
        SERVER_NOT_FOUND,
        ALREADY_CONNECTED,
        CONNECTION_CANCELLED,
        CONNECTION_IN_PROGRESS,
        SERVER_DISCONNECTED,
        UNKNOWN_ERROR,
        SUCCESS;
    }

    fun isSuccessful(): Boolean = this.status == Status.SUCCESS
}