package dev.slne.surf.core.api.common.player.serializer

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.OffsetDateTimeSerializer
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfProxyServer
import dev.slne.surf.core.api.common.server.SurfServer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.InetAddress
import java.time.OffsetDateTime
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
object SurfPlayerSerializer : KSerializer<SurfPlayer> {
    override val descriptor = buildClassSerialDescriptor("SurfPlayer") {
        element<String>("uuid")
        element<String?>("lastKnownName", isOptional = true)
        element<Long?>("firstSeen", isOptional = true)
        element<Long?>("lastSeen", isOptional = true)
        element<String?>("currentServer", isOptional = true)
        element<String?>("currentProxy", isOptional = true)
        element<String?>("lastKnownIpAddress", isOptional = true)
        element<Boolean>("transferred")
    }

    override fun serialize(encoder: Encoder, value: SurfPlayer) {
        val composite = encoder.beginStructure(descriptor)

        composite.encodeStringElement(descriptor, 0, value.uuid.toString())
        composite.encodeNullableSerializableElement(
            descriptor,
            1,
            String.serializer(),
            value.lastKnownName
        )
        composite.encodeNullableSerializableElement(
            descriptor,
            2,
            OffsetDateTimeSerializer,
            value.firstSeen
        )
        composite.encodeNullableSerializableElement(
            descriptor,
            3,
            OffsetDateTimeSerializer,
            value.lastSeen
        )
        composite.encodeNullableSerializableElement(
            descriptor,
            4,
            String.serializer(),
            value.currentServer?.name
        )
        composite.encodeNullableSerializableElement(
            descriptor,
            5,
            String.serializer(),
            value.currentProxy?.name
        )
        composite.encodeNullableSerializableElement(
            descriptor,
            6,
            String.serializer(),
            value.lastKnownIpAddress?.hostAddress
        )
        composite.encodeBooleanElement(descriptor, 7, value.transferred)

        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): SurfPlayer {
        val dec = decoder.beginStructure(descriptor)

        var uuid: UUID? = null
        var lastKnownName: String? = null
        var firstSeen: OffsetDateTime? = null
        var lastSeen: OffsetDateTime? = null
        var currentServer: SurfServer? = null
        var currentProxy: SurfProxyServer? = null
        var lastKnownIpAddress: InetAddress? = null
        var transferred: Boolean = false

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> uuid = UUID.fromString(dec.decodeStringElement(descriptor, 0))
                1 -> lastKnownName =
                    dec.decodeNullableSerializableElement(descriptor, 1, String.serializer())

                2 -> firstSeen =
                    dec.decodeNullableSerializableElement(descriptor, 2, OffsetDateTimeSerializer)

                3 -> lastSeen =
                    dec.decodeNullableSerializableElement(descriptor, 3, OffsetDateTimeSerializer)

                4 -> currentServer =
                    dec.decodeNullableSerializableElement(descriptor, 4, String.serializer())
                        ?.let(SurfCoreApi::getServerByName)

                5 -> currentProxy =
                    dec.decodeNullableSerializableElement(descriptor, 5, String.serializer())
                        ?.let(SurfCoreApi::getProxyServerByName)

                6 -> lastKnownIpAddress =
                    dec.decodeNullableSerializableElement(descriptor, 6, String.serializer())
                        ?.let(InetAddress::getByName)

                7 -> transferred = dec.decodeBooleanElement(descriptor, 7)

                CompositeDecoder.DECODE_DONE -> break@loop
                else -> error("Unknown index $index")
            }
        }

        dec.endStructure(descriptor)

        return SurfPlayer(
            uuid = uuid ?: error("uuid missing"),
            lastKnownName = lastKnownName,
            firstSeen = firstSeen,
            lastSeen = lastSeen,
            currentServer = currentServer,
            currentProxy = currentProxy,
            lastKnownIpAddress = lastKnownIpAddress,
            transferred = transferred
        )
    }
}