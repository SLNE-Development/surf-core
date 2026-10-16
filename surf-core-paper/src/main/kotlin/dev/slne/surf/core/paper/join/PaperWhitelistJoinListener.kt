package dev.slne.surf.core.paper.join

import dev.slne.surf.core.api.common.join.CoreJoinListener
import dev.slne.surf.core.api.common.join.CoreJoinResult
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

object PaperWhitelistJoinListener : CoreJoinListener {
    override val name = "whitelist"

    override suspend fun checkJoin(playerUuid: UUID): CoreJoinResult {
        if (!Bukkit.hasWhitelist()) {
            return CoreJoinResult.allowed()
        }

        val player = Bukkit.getOfflinePlayer(playerUuid)
        if (player.isWhitelisted || player.isOp) {
            return CoreJoinResult.allowed()
        }

        return CoreJoinResult.denied(Component.text("Du stehst nicht auf der Whitelist"))
    }
}
