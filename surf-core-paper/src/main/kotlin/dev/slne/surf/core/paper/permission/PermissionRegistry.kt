package dev.slne.surf.core.paper.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry
import dev.slne.surf.core.core.common.permission.CorePermissions

object PermissionRegistry : PermissionRegistry() {
    const val BASE = CorePermissions.BASE
    const val BASE_COMMAND = CorePermissions.BASE_COMMAND

    val COMMAND_LAST_SEEN = create(CorePermissions.COMMAND_LAST_SEEN)
    val COMMAND_NETWORK_LIST = create(CorePermissions.COMMAND_NETWORK_LIST)
    val COMMAND_NETWORK_TELEPORT = create(CorePermissions.COMMAND_NETWORK_TELEPORT)
    val COMMAND_WHERE_AM_I = create(CorePermissions.COMMAND_WHERE_AM_I)
    val COMMAND_NETWORK_SERVER = create(CorePermissions.COMMAND_NETWORK_SERVER)
    val COMMAND_NETWORK_BROADCAST = create(CorePermissions.COMMAND_NETWORK_BROADCAST)
    val COMMAND_NETWORK_SEND = create(CorePermissions.COMMAND_NETWORK_SEND)
    val COMMAND_NETWORK_SERVER_MAX_PLAYERS = create(CorePermissions.COMMAND_NETWORK_SERVER_MAX_PLAYERS)
    val COMMAND_HUB = create(CorePermissions.COMMAND_HUB)

    val BYPASS_MAX_PLAYERS = create(CorePermissions.BYPASS_MAX_PLAYERS)

    val COMMAND_INFO = create(CorePermissions.COMMAND_INFO)
    val COMMAND_INFO_PLAYER = create(CorePermissions.COMMAND_INFO_PLAYER)
    val COMMAND_INFO_SERVER = create(CorePermissions.COMMAND_INFO_SERVER)

    val COMMAND_CORE = create(CorePermissions.COMMAND_CORE)

    val JOIN_WHERE_AM_I = create(CorePermissions.JOIN_WHERE_AM_I)
}
