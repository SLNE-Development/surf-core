package dev.slne.surf.core.api.common.server.state

enum class ExternalSurfServerState {
    STARTING,
    ONLINE,
    STOPPING,
    OFFLINE,
    UNREACHABLE,
    CRASHED
}