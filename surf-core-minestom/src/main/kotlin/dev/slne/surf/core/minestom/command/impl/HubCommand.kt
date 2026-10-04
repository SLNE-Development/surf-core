package dev.slne.surf.core.minestom.command.impl

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.minestom.util.surfPlayer
import dev.slne.surf.core.core.common.command.HubCommandHandler
import dev.slne.surf.core.core.common.permission.CorePermissions

fun hubCommand() = commandTree("hub") {
    withAliases("lobby", "l")
    withPermission(CorePermissions.COMMAND_HUB)

    playerExecutorSuspend { player, _ ->
        HubCommandHandler.sendToHub(player, player.surfPlayer)
    }
}
