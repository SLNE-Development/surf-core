package dev.slne.surf.core.minestom

import com.google.auto.service.AutoService
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.CoreInstance

@AutoService(CoreInstance::class)
class MinestomCoreInstance : ClientCoreInstance {
    override val clientLoader = ClientLoader(SurfCoreMinestomEntrypoint.dataPath)
}
