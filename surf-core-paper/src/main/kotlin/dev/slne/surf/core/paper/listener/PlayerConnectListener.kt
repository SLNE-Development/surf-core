package dev.slne.surf.core.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.core.api.common.server.connection.SurfProxyServerConnectionResult
import dev.slne.surf.core.api.paper.util.surfPlayer
import dev.slne.surf.core.core.common.player.surfPlayerService
import dev.slne.surf.core.core.common.redis.redisApi
import dev.slne.surf.core.core.common.redis.request.SendPlayerToProxyRequest
import dev.slne.surf.core.core.common.redis.watcher.PlayerProxyConnectionResultWatcher
import dev.slne.surf.core.core.common.util.formatMillis
import dev.slne.surf.core.paper.permission.PermissionRegistry
import dev.slne.surf.core.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.command.util.idOrThrow
import dev.slne.surf.surfapi.core.api.messages.adventure.*
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent

object PlayerConnectListener : Listener {
    private val luckperms by lazy {
        LuckPermsProvider.get()
    }

    @EventHandler
    fun onPlayerClientLoaded(event: PlayerClientLoadedWorldEvent) {
        val player = event.player
        val surfPlayer = surfPlayerService.findPlayerByUuid(player.uniqueId)

        if (surfPlayer == null) {
            player.kick(buildDisconnectComponent(), PlayerKickEvent.Cause.UNKNOWN)
            return
        }

        if (!event.player.hasPermission(PermissionRegistry.JOIN_WHERE_AM_I)) {
            return
        }

        player.sendText {
            appendInfoPrefix()
            info("Du, ")
            append {
                variableValue(player.name)
                hoverEvent(buildText {
                    variableValue(player.uniqueId.toString())
                })
                clickCopiesToClipboard(player.uniqueId.toString())
            }
            info(", befindest dich momentan, ")
            append {
                spacer("(${System.currentTimeMillis().formatMillis()})")
                clickCopiesToClipboard(System.currentTimeMillis().toString())
            }
            info(", auf dem Server ")
            variableValue(surfPlayer.currentServer?.name ?: "Unbekannt")
            info(" auf dem Proxy ")
            variableValue(surfPlayer.currentProxy?.name ?: "Unbekannt")
            info(".")
        }
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun onPlayerConnect(event: AsyncPlayerConnectionConfigureEvent) {
        val surfPlayer =
            event.connection.audience.uuidOrNull()?.let { surfPlayerService.findPlayerByUuid(it) }

        if (surfPlayer == null) {
            plugin.logger.severe("Failed to load player data for player with UUID ${event.connection.audience.uuidOrNull()}. The player will be disconnected.")
            event.connection.disconnect(buildDisconnectComponent())
        }
    }

    @EventHandler
    fun onPlayerServerFullCheckEvent(event: PlayerServerFullCheckEvent) {
        val uuid = event.playerProfile.idOrThrow()
        val user = luckperms.userManager.getUser(uuid) ?: return
        val hasBypass = user.cachedData.permissionData.checkPermission(
            PermissionRegistry.BYPASS_MAX_PLAYERS
        ).asBoolean()

        if (hasBypass) {
            event.allow(true)
        }
    }

    @EventHandler
    fun onTransferred(event: PlayerJoinEvent) {
        val player = event.player.surfPlayer

        if (!player.transferred) {
            return
        }

        redisApi.publishEvent(
            SendPlayerToProxyRequest.Response(
                player.uuid,
                SurfProxyServerConnectionResult(SurfProxyServerConnectionResult.Status.SUCCESS)
            )
        )

        plugin.launch {
            PlayerProxyConnectionResultWatcher.cleanUp(player.uuid)
        }
    }

    private fun buildDisconnectComponent() = buildText {
        appendNewline(2)
        primary("CASTCRAFTER")
        appendNewline()
        primary("COMMUNITY SERVER")
        appendNewline(2)
        error("DEINE SPIELERDATEN KONNTEN NICHT GELADEN WERDEN.")
        appendNewline()
        error("Code: 1921186215185: 1") // surf-core in A1Z26-Cipher + Error Code 1
        appendNewline(3)
        spacer("Beim laden deiner Spielerdaten ist ein interner Fehler aufgetreten.")
        appendNewline()
        spacer("Sollte das Problem weiterhin bestehen, wende dich bitte an den Support.")
        appendNewline(2)
        primary("discord.gg/castcrafter")
    }
}