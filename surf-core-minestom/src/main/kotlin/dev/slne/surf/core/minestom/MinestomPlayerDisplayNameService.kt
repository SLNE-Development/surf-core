package dev.slne.surf.core.minestom

import dev.slne.surf.api.core.minimessage.miniMessage
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

object MinestomPlayerDisplayNameService {
    private var subscription: EventSubscription<UserDataRecalculateEvent>? = null

    fun start() {
        val luckPerms = LuckPermsProvider.get()
        subscription = luckPerms.eventBus.subscribe(UserDataRecalculateEvent::class.java) { event ->
            val player = MinecraftServer.getConnectionManager()
                .getOnlinePlayerByUuid(event.user.uniqueId) ?: return@subscribe
            player.scheduleNextTick { update(player) }
        }
    }

    fun stop() {
        subscription?.close()
        subscription = null
    }

    fun update(player: Player) {
        val prefix = LuckPermsProvider.get().userManager.getUser(player.uuid)?.cachedData
            ?.metaData?.prefix.orEmpty()
        player.displayName = miniMessage.deserialize(prefix + player.username)
    }
}
