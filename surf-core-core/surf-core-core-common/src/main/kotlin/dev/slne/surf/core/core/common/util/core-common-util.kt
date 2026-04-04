package dev.slne.surf.core.core.common.util

import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zone = ZoneId.of("Europe/Berlin")

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun Long.formatMillis(): String =
    Instant.ofEpochMilli(this)
        .atZone(zone)
        .format(dateTimeFormatter)

fun Long.formatDateMillis(): String =
    Instant.ofEpochMilli(this)
        .atZone(zone)
        .format(dateFormatter)

fun Long.formatTimeMillis(): String =
    Instant.ofEpochMilli(this)
        .atZone(zone)
        .format(timeFormatter)

fun SurfComponentBuilder.appendCorePrefix() = append {
    spacer(">>")
    appendSpace()
    primary("Core")
    appendSpace()
    darkSpacer("|")
    appendSpace()
}


fun OffsetDateTime.formatDateTime(
    formatter: DateTimeFormatter = dateTimeFormatter
): String = this.format(formatter)