package dev.slne.surf.core.paper

import com.google.auto.service.AutoService
import dev.slne.surf.core.client.ClientCoreInstance
import dev.slne.surf.core.client.ClientLoader
import dev.slne.surf.core.core.CoreInstance

@AutoService(CoreInstance::class)
class PaperCoreInstance : ClientCoreInstance {
    override val clientLoader: ClientLoader = ClientLoader(PaperBootstrap.context.dataDirectory)
}