package dev.slne.surf.core.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.core"
    const val BASE_COMMAND = "$BASE.command"

    val COMMAND_LAST_SEEN = create("$BASE_COMMAND.lastseen")
    val COMMAND_NETWORK_LIST = create("$BASE_COMMAND.networklist")
    val COMMAND_NETWORK_TELEPORT = create("$BASE_COMMAND.networkteleport")
    val COMMAND_WHERE_AM_I = create("$BASE_COMMAND.whereami")
    val COMMAND_NETWORK_SERVER = create("$BASE_COMMAND.networkserver")
    val COMMAND_NETWORK_BROADCAST = create("$BASE_COMMAND.networkbroadcast")

    val COMMAND_INFO = create("$BASE_COMMAND.info")
    val COMMAND_INFO_PLAYER = create("$BASE_COMMAND.info.player")
    val COMMAND_INFO_SERVER = create("$BASE_COMMAND.info.server")

    val COMMAND_CORE = create("$BASE_COMMAND.core")

    val JOIN_WHERE_AM_I = create("$BASE.whereamijoin")
}