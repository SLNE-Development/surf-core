package dev.slne.surf.core.velocity.permission

object PermissionList {
    private const val BASE = "surf.core"

    const val CORE_COMMAND = "$BASE.command"

    const val CORE_COMMAND_PLAYER = "$CORE_COMMAND.player"

    const val CORE_COMMAND_SERVICE = "$CORE_COMMAND.service"

    val CORE_COMMAND_TOGGLE_SERVICE_STATUS_MESSAGES =
        "$CORE_COMMAND.togglecoreservicestatusmessages"
}