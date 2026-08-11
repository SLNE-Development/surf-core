package dev.slne.surf.core.api.minestom.command.argument

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

/**
 * A lazily resolved offline player command argument.
 *
 * The lookup stays asynchronous while command handlers receive a dedicated type instead of the
 * raw command input.
 */
class SurfOfflinePlayerArgument @ApiStatus.Internal constructor(
    private val playerName: String,
) {
    suspend fun resolve(): SurfPlayer? = playerName.toUuidOrNull()
        ?.let { SurfCoreApi.getOfflinePlayer(it) }
        ?: SurfCoreApi.getOfflinePlayer(playerName)
}

private fun String.toUuidOrNull(): UUID? {
    if (length <= 16) return null // Shorter than a UUID, likely a username
    return runCatching(UUID::fromString).getOrNull()
}