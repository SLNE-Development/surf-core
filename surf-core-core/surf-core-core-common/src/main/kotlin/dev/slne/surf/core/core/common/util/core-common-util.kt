package dev.slne.surf.core.core.common.util

import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
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


fun OffsetDateTime.formatDateTime(
    formatter: DateTimeFormatter = dateTimeFormatter
): String = this.format(formatter)


fun SurfComponentBuilder.renderDisconnectMessage(
    titleReason: String,
    reason: SurfComponentBuilder.() -> Unit,
    footer: SurfComponentBuilder.() -> Unit = { spacer("Sollte das Problem weiterhin bestehen, wende dich bitte an den Support.") }
) =
    buildText {
        appendNewline(2)
        primary("CASTCRAFTER")
        appendNewline()
        primary("COMMUNITY SERVER")
        appendNewline(2)
        error(titleReason)
        appendNewline(3)
        append(reason)
        appendNewline()
        append(footer)
        appendNewline(2)
        primary("discord.gg/castcrafter")
    }