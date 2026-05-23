package dev.slne.surf.core.launcher.server

object CoreLauncherEnvironment {
    val MC_MEMORY_MAX =
        System.getenv("SERVER_MEMORY") ?: error("SERVER_MEMORY environment variable is not set")
    val MC_MEMORY_MIN = System.getenv("SERVER_MEMORY_MIN")
        ?: error("SERVER_MEMORY_MIN environment variable is not set")
    val SERVER_PORT = System.getenv("SERVER_PORT")?.toInt()
        ?: error("SERVER_PORT environment variable is not set or is not a valid integer")
    
    val STARTUP_COMMAND = System.getenv("STARTUP")
        ?: error("STARTUP environment variable is not set")
}