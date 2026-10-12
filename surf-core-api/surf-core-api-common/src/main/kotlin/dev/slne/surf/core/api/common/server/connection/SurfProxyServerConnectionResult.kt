package dev.slne.surf.core.api.common.server.connection

import dev.slne.surf.core.api.common.join.SurfJoinCheckResult
import kotlinx.serialization.Serializable

@Serializable
data class SurfProxyServerConnectionResult(
    val status: Status
) {
    var joinCheck: SurfJoinCheckResult? = null
        private set

    constructor(status: Status, joinCheck: SurfJoinCheckResult?) : this(status) {
        this.joinCheck = joinCheck
    }

    enum class Status {
        ERR_UNKNOWN,
        ALREADY_CONNECTED,
        ALREADY_TRANSFERRING,
        SERVER_NOT_FOUND,
        SUCCESS,
        JOIN_DENIED;
    }

    fun isSuccessful(): Boolean = this.status == Status.SUCCESS
}
