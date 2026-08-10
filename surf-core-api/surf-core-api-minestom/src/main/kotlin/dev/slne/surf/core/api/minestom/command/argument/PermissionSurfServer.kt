package dev.slne.surf.core.api.minestom.command.argument

/**
 * Restricts a [dev.slne.surf.core.api.common.server.CommonSurfServer] argument to servers the
 * executing player may join.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PermissionSurfServer
