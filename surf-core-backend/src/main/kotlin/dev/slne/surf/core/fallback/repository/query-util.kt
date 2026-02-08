package dev.slne.surf.core.fallback.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and

fun Op<Boolean>?.andCondition(newCondition: Op<Boolean>): Op<Boolean> =
    this?.let { it and newCondition } ?: newCondition
