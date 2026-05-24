package dev.slne.surf.core.launcher.server.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*
import kotlin.io.path.Path

@ConfigSerializable
data class CoreLauncherConfig(
    val serverStartupCommand: String = "java -jar server.jar --nogui",
    val startedMessage: String = "For help, type \"help\"",
    val serverName: String = "unknown",
    val serverDisplayName: String = "unknown",
    val serverCategory: String = "unknown",
    val serverUuid: SerializableUUID = UUID.randomUUID()
) {
    companion object : SpongeYmlConfigClass<CoreLauncherConfig>(
        CoreLauncherConfig::class.java,
        Path("."),
        "core-launcher-config.yml"
    )
}
