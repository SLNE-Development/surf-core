package dev.slne.surf.core.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.CoreInstance

@AutoService(CoreInstance::class)
class VelocityCoreInstance : ClientCoreInstance {
    override val clientLoader: ClientLoader = ClientLoader(plugin.dataPath)
}