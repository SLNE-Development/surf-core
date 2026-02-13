package dev.slne.surf.core.api.common.server.connection

enum class SurfServerConnectResult {
    SERVER_NOT_FOUND,
    ALREADY_CONNECTED,
    CONNECTION_CANCELLED,
    CONNECTION_IN_PROGRESS,
    SERVER_DISCONNECTED,
    SUCCESS;

    fun isSuccessful(): Boolean = this == SUCCESS
}