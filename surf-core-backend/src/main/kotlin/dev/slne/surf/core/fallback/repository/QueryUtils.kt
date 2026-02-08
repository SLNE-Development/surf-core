package dev.slne.surf.core.fallback.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and

/**
 * Helper extension function to chain WHERE conditions.
 * If the receiver is null, returns the new condition.
 * Otherwise, combines the existing condition with the new one using AND.
 */
internal fun Op<Boolean>?.andCondition(newCondition: Op<Boolean>): Op<Boolean> =
    this?.let { it and newCondition } ?: newCondition
