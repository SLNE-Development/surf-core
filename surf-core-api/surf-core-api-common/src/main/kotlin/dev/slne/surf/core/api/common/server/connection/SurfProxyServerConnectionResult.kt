package dev.slne.surf.core.api.common.server.connection

import kotlinx.serialization.Serializable

@Serializable
data class SurfProxyServerConnectionResult(
    val status: Status
) {
    enum class Status {
        ERR_UNKNOWN,
        ALREADY_CONNECTED,
        ALREADY_TRANSFERRING,
        SERVER_NOT_FOUND,
        SUCCESS;
    }

    fun isSuccessful(): Boolean = this.status == Status.SUCCESS
}