package com.dddheroes.cinema.modules.dayschedule

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlin.ranges.random

val year = (1..294276).random()
val month = (1..12).random()
val day = (1..28).random()
val generated = AtomicInteger(0)

fun DayScheduleId.Companion.random(): DayScheduleId = DayScheduleId.of(
    randomDate()
)

fun randomDate(hour: Int, minute: Int): Instant = randomDate().atTime(hour, minute).toInstant(ZoneOffset.UTC)

fun randomDate(): LocalDate = LocalDate
    .of(year, month, day).plusDays(generated.getAndIncrement().toLong())