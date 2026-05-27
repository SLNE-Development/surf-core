package dev.slne.surf.core.velocity.permission

object PermissionList {
    private const val BASE = "surf.core"

    const val CORE_COMMAND = "$BASE.command"
    const val CORE_COMMAND_PLAYER = "$CORE_COMMAND.player"
    const val CORE_COMMAND_SERVICE = "$CORE_COMMAND.service"

    const val BYPASS_PERMISSION = "$BASE.bypass"
    const val TEAM_PERMISSION = "$BASE.team"
}