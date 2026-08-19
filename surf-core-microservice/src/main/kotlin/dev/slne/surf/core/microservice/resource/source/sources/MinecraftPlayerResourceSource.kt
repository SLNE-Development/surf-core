package dev.slne.surf.core.microservice.resource.source.sources

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.microservice.resource.source.RemotePlayerResourceSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object MinecraftPlayerResourceSource : RemotePlayerResourceSource {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val log = logger()

    override suspend fun findPlayerSkin(playerUuid: UUID): String? = runCatching {
        val id = playerUuid.toString().replace("-", "")
        val request = Request.Builder()
            .url("https://sessionserver.mojang.com/session/minecraft/profile/$id")
            .build()

        client.await(request).use { response ->
            if (!response.isSuccessful) return null
            val body = response.body.string().takeIf { it.isNotBlank() } ?: return null

            return json.decodeFromString<ProfileResponse>(body)
                .properties
                .firstOrNull { it.name == "textures" }
                ?.value
        }
    }.onFailure {
        log.atWarning().log("Failed to fetch player skin for UUID $playerUuid: ${it.message}")
    }.getOrNull()

    private suspend fun OkHttpClient.await(request: Request): Response =
        suspendCancellableCoroutine { cont ->
            val call = newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) = cont.resume(response)
                override fun onFailure(call: Call, e: IOException) = cont.resumeWithException(e)
            })
        }

    @Serializable
    private data class ProfileResponse(val properties: List<Property> = emptyList())

    @Serializable
    private data class Property(val name: String, val value: String)
}