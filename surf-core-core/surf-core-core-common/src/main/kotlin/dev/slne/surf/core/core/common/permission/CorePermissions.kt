package dev.slne.surf.core.core.common.permission

object CorePermissions {
    const val BASE = "surf.core"
    const val BASE_COMMAND = "$BASE.command"

    const val COMMAND_LAST_SEEN = "$BASE_COMMAND.lastseen"
    const val COMMAND_NETWORK_LIST = "$BASE_COMMAND.networklist"
    const val COMMAND_NETWORK_TELEPORT = "$BASE_COMMAND.networkteleport"
    const val COMMAND_WHERE_AM_I = "$BASE_COMMAND.whereami"
    const val COMMAND_NETWORK_SERVER = "$BASE_COMMAND.networkserver"
    const val COMMAND_NETWORK_BROADCAST = "$BASE_COMMAND.networkbroadcast"
    const val COMMAND_NETWORK_SEND = "$BASE_COMMAND.networksend"
    const val COMMAND_NETWORK_SERVER_MAX_PLAYERS = "$BASE_COMMAND.nservermaxplayers"
    const val COMMAND_HUB = "$BASE_COMMAND.hub"

    const val BYPASS_MAX_PLAYERS = "$BASE.bypassmaxplayers"

    const val COMMAND_INFO = "$BASE_COMMAND.info"
    const val COMMAND_INFO_PLAYER = "$BASE_COMMAND.info.player"
    const val COMMAND_INFO_SERVER = "$BASE_COMMAND.info.server"

    const val COMMAND_CORE = "$BASE_COMMAND.core"
    const val JOIN_WHERE_AM_I = "$BASE.whereamijoin"
    const val SERVER_NOTIFY = "$BASE.servernotify"
    const val SERVER_WILDCARD = "$BASE.server.*"

    fun server(name: String) = "$BASE.server.$name"
}
