package dev.slne.surf.core.launcher.server

object CoreLauncherEnvironment {
    val MC_MEMORY_MAX =
        System.getenv("SERVER_MEMORY") ?: error("SERVER_MEMORY environment variable is not set")
    val SERVER_PORT = System.getenv("SERVER_PORT")?.toInt()
        ?: error("SERVER_PORT environment variable is not set or is not a valid integer")
}