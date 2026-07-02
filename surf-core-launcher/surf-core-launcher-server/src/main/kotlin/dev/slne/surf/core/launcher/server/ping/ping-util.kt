package dev.slne.surf.core.launcher.server.ping

@Suppress("SameParameterValue")
fun buildHandshakePacket(
    host: String,
    port: Int
): ByteArray {
    val hostBytes = host.toByteArray()

    val data = mutableListOf<Byte>()

    data += 0x00
    data += writeVarInt(763).toList()

    data += writeVarInt(hostBytes.size).toList()
    data += hostBytes.toList()

    data += ((port shr 8) and 0xFF).toByte()
    data += (port and 0xFF).toByte()

    data += 0x01

    val packetLength = writeVarInt(data.size)

    return packetLength + data.toByteArray()
}

fun writeVarInt(value: Int): ByteArray {
    var current = value
    val output = mutableListOf<Byte>()

    do {
        var temp = (current and 0b01111111)

        current = current ushr 7

        if (current != 0) {
            temp = temp or 0b10000000
        }

        output += temp.toByte()
    } while (current != 0)

    return output.toByteArray()
}