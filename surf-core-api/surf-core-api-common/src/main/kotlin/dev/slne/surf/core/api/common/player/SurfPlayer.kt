package dev.slne.surf.core.api.common.player

import dev.slne.surf.core.api.common.player.name.NameHistory
import org.gradle.api.component.Component
import java.util.UUID

data class SurfPlayer(
    val uuid: UUID,
    val firstSeen: Long?,
    val lastSeen: Long?,
    val nameHistory: NameHistory
)