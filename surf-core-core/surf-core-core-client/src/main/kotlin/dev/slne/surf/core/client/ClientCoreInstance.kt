package dev.slne.surf.core.client

import com.google.auto.service.AutoService
import dev.slne.surf.core.core.CoreInstance
import dev.slne.surf.redis.RedisApi

@AutoService(CoreInstance::class)
class ClientCoreInstance : CoreInstance {
    override val redisApi: RedisApi get() = ClientLoader.redisApi
}