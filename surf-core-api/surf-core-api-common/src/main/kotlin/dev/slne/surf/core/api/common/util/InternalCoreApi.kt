package dev.slne.surf.core.api.common.util

/**
 * Marks declarations that are internal to the Surf Core API implementation.
 *
 * This annotation indicates that the marked API is intended for internal use only
 * and is not part of the stable public API. Internal APIs may be changed or removed
 * at any time without notice, even in minor version updates.
 *
 * Using internal APIs may lead to:
 * - Binary incompatibilities in future versions
 * - Unexpected behavior or crashes
 * - Breaking changes without deprecation warnings
 *
 * @see RequiresOptIn
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal to the Surf Core implementation and is not intended for external use. " +
            "It may be changed or removed without notice in future versions."
)
annotation class InternalCoreApi