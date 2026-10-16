package dev.slne.surf.core.api.common.join

import net.kyori.adventure.text.Component

sealed interface CoreJoinResult {
    data object Allowed : CoreJoinResult
    data class Denied(val reason: Component? = null) : CoreJoinResult

    companion object {
        fun allowed(): CoreJoinResult = Allowed
        fun denied(reason: Component? = null): CoreJoinResult = Denied(reason)
    }
}
