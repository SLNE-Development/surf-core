package dev.slne.surf.core.core.common.util

import java.time.Instant
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun Long.formatMillis() = dateTimeFormatter.format(Instant.ofEpochMilli(this))
fun Long.formatDateMillis() = dateFormatter.format(Instant.ofEpochMilli(this))
fun Long.formatTimeMillis() = timeFormatter.format(Instant.ofEpochMilli(this))