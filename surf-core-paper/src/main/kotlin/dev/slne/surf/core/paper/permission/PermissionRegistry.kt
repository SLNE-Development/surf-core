package dev.slne.surf.core.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.core"
    const val BASE_COMMAND = "$BASE.command"

    val COMMAND_LAST_SEEN = create("$BASE_COMMAND.lastseen")
}